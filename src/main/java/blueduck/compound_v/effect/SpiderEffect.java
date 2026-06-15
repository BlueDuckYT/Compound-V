package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import blueduck.compound_v.entity.WebProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spider powers (Spider-Man style), one active web at a time.
 *
 * - Tap V: fire a web projectile. Sticks to a block (anchor) or latches a mob.
 * - Hold V (after firing): keeps the most-recent web connection LIVE.
 *     * Block anchor -> roped: gravity + rope-LENGTH CONSTRAINT = real pendulum swing.
 *       Scroll shortens/lengthens the rope (reel in, gain height, or drop).
 *     * Mob latch -> scroll reels the mob toward you.
 * - Release V: cut the web; you keep your momentum (apex release).
 * - Auto-cling wall climbing via block-probe (reliable): look up / jump to climb,
 *   sneak to descend, drop to release.
 */
public class SpiderEffect extends CompoundVEffect {

    private static final DustParticleOptions WEB_PARTICLE = new DustParticleOptions(
            new Vector3f(0.9f, 0.9f, 0.9f), 0.4f);

    private static class SpiderState {
        int activeWebId = -1;
        long lastHeldTick = -100;
        double ropeLength = -1;
        boolean wasRoped = false;
        long lastFireTick = -100; // cooldown gate for firing webs
        boolean releasedInFlight = false; // V released before the web stuck -> cut on stick
        long lastSenseCue = -1000;        // last tick a spider-sense warning cue played
        long senseWindowUntil = -1;       // reaction (damage-reduction) window active until this tick
        long fastReelUntil = -1;          // latched mob was reeled in fast; "primed" window until this tick
        boolean punchPrimed = false;      // player landed a hit on the reeled mob -> fling on release
        java.util.UUID flingArmedMob = null; // mob reeled in then released — punch soon to fling it
        long flingArmedUntil = -1;           // window during which flingArmedMob can be punch-flung
    }

    private static final Map<UUID, SpiderState> stateMap = new ConcurrentHashMap<>();

    public SpiderEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    private static SpiderState state(UUID uuid) {
        return stateMap.computeIfAbsent(uuid, k -> new SpiderState());
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        if (CompoundVEffect.arePowersSuppressed(player)) return;
        SpiderState s = state(player.getUUID());
        long now = level.getGameTime();

        // Fire ONLY on a genuine new press. While V is held, holdActivate keeps
        // lastHeldTick current; if a hold is still in progress (a hold packet arrived
        // within the tolerance window) we must NOT fire another web — holding keeps the
        // EXISTING web connected, it does not spawn new ones. This is what stops the
        // hold-to-spam behaviour at the source. consumeClick can also double-fire within
        // a tick on some setups, so the short fire cooldown is a secondary guard.
        boolean holdInProgress = (now - s.lastHeldTick) <= 3 && s.activeWebId != -1;
        if (holdInProgress) {
            s.lastHeldTick = now; // keep the connection alive
            return;
        }
        if (now - s.lastFireTick < Config.spiderFireCooldown) {
            s.lastHeldTick = now;
            return;
        }
        s.lastFireTick = now;

        discardActiveWeb(level, s);

        WebProjectileEntity web = new WebProjectileEntity(level, player);
        web.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                (float) Config.spiderWebSpeed, 0.5F);
        level.addFreshEntity(web);
        s.activeWebId = web.getId();
        s.ropeLength = -1;
        s.lastHeldTick = now;
        s.releasedInFlight = false;

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.5F, 1.8F);
    }

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        if (CompoundVEffect.arePowersSuppressed(player)) return;
        state(player.getUUID()).lastHeldTick = level.getGameTime();
    }

    @Override
    public void onRelease(ServerPlayer player, int amplifier, ServerLevel level) {
        // Cut the web on V release. If it has already stuck, discard it now. If it's still in
        // flight (a quick TAP), flag it so it's cut the moment it sticks — this is what makes
        // a tap a one-shot while a hold stays connected.
        SpiderState s = stateMap.get(player.getUUID());
        if (s == null || s.activeWebId == -1) return;
        WebProjectileEntity web = activeWeb(level, s);

        // If we're releasing while a mob is latched and freshly reeled in, ARM the punch-fling:
        // the player can now release V and THEN punch the mob to fling it. This is the intuitive
        // order (let go of the web, then hit them).
        if (web != null && web.isStuck() && web.isOnMob()
                && level.getGameTime() <= s.fastReelUntil) {
            Entity tgt = level.getEntity(web.getStuckEntity());
            if (tgt instanceof LivingEntity) {
                s.flingArmedMob = tgt.getUUID();
                s.flingArmedUntil = level.getGameTime() + 20; // ~1s to land the punch after release
            }
        }

        s.punchPrimed = false;
        s.fastReelUntil = -1;

        if (web != null && web.isStuck()) {
            discardActiveWeb(level, s);
            s.releasedInFlight = false;
        } else {
            s.releasedInFlight = true; // cut it as soon as it sticks
        }
    }

    /**
     * Called from ForgeEvents when a Spider player attacks an entity. Two ways to fling:
     *  - Release-then-punch (the intuitive default): reel a mob in, let go of V, then hit it.
     *    The release arms the fling for ~1s; the punch flings the armed mob.
     *  - Punch-while-held: hitting a latched, freshly-reeled mob also flings it.
     * Either way the mob is hurled in the player's look direction at the moment of the hit,
     * with bonus damage.
     */
    public static void notifyAttack(ServerPlayer player, Entity victim) {
        SpiderState s = stateMap.get(player.getUUID());
        if (s == null) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!(victim instanceof LivingEntity mob)) return;
        long now = level.getGameTime();

        boolean armedHit = victim.getUUID().equals(s.flingArmedMob) && now <= s.flingArmedUntil;

        boolean heldHit = false;
        if (s.activeWebId != -1) {
            WebProjectileEntity web = activeWeb(level, s);
            if (web != null && web.isOnMob() && now <= s.fastReelUntil
                    && victim.getUUID().equals(web.getStuckEntity())) {
                heldHit = true;
            }
        }

        if (!armedHit && !heldHit) return;

        // Fling NOW, in the punch-moment look direction.
        Vec3 look = player.getLookAngle();
        mob.setDeltaMovement(look.scale(Config.spiderFlingForce).add(0, 0.3, 0));
        mob.hurtMarked = true;
        mob.fallDistance = 0;
        mob.invulnerableTime = 0;
        mob.hurt(player.damageSources().playerAttack(player), (float) Config.spiderFlingDamage);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 14, 0.3, 0.3, 0.3, 0.2);
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 1.0F);

        // Consume all fling state and cut any web so the mob flies free.
        s.fastReelUntil = -1;
        s.punchPrimed = false;
        s.flingArmedMob = null;
        s.flingArmedUntil = -1;
        if (s.activeWebId != -1) discardActiveWeb(level, s);
    }

    @Override
    public boolean usesScroll(ServerPlayer player) {
        SpiderState s = stateMap.get(player.getUUID());
        return s != null && s.activeWebId != -1;
    }

    @Override
    public void scrollAdjust(ServerPlayer player, int amplifier, ServerLevel level, int dir) {
        SpiderState s = state(player.getUUID());
        WebProjectileEntity web = activeWeb(level, s);
        if (web == null || !web.isStuck()) return;

        if (web.isOnMob()) {
            Entity tgt = level.getEntity(web.getStuckEntity());
            if (tgt instanceof LivingEntity mob && dir < 0) { // scroll DOWN pulls mob toward you
                Vec3 toPlayer = player.position().add(0, 0.4, 0).subtract(mob.position());
                double d = toPlayer.length();
                if (d > 1.0) {
                    double speed = Math.min(1.5, d * 0.25 + 0.3);
                    Vec3 vel = toPlayer.normalize().scale(speed);
                    mob.setDeltaMovement(vel);
                    mob.hurtMarked = true;
                    mob.fallDistance = 0;
                    // If reeled in toward you and now reasonably close, "prime" a window: punch
                    // it during the window and it's flung. Generous thresholds so the combo is
                    // easy to land consistently.
                    if (speed > 0.5 && d < 7.0) {
                        s.fastReelUntil = level.getGameTime() + 40; // ~2s window
                    }
                }
            }
        } else {
            if (s.ropeLength < 0) {
                s.ropeLength = player.position().distanceTo(web.getStuckPos());
            }
            // dir: scroll UP = +1, scroll DOWN = -1.
            // Scroll DOWN shortens the rope (reel in / gain height); scroll UP adds slack.
            s.ropeLength = Math.max(Config.spiderMinRope,
                    Math.min(Config.spiderMaxRope, s.ropeLength + dir * Config.spiderReelStep));
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;
        SpiderState s = state(player.getUUID());

        WebProjectileEntity web = activeWeb(level, s);

        // Connection model:
        //   TAP V  -> a short press: the web is a one-shot projectile. It still hits a
        //            mob (root effects on impact) or a block, then the explicit release
        //            (onRelease, key-up) cuts it. No swinging / yanking.
        //   HOLD V -> the web stays CONNECTED while V is held. The block swing is driven
        //            CLIENT-SIDE for smoothness; the server just keeps the connection alive
        //            and maintains rope/fall bookkeeping. A mob latch is reeled via scroll.
        //   RELEASE V -> onRelease (key-up packet) cuts the web immediately.
        //
        // IMPORTANT: we do NOT discard here based on the hold-timeout window. The hold packet
        // arrives on the client tick and can briefly lag the server tick by more than the old
        // 3-tick tolerance, which was cutting a held web mid-swing. Release is now an explicit
        // signal (onRelease), so a gap in hold packets must NOT drop the web.
        boolean ropedThisTick = false;

        // Hold backstop: if V hasn't been held within a generous window, detach and despawn
        // any ATTACHED web. onRelease (key-up) handles the instant clean case; this guarantees
        // an attached web never persists when you're not holding V, even if a release packet
        // was missed or the web stuck after release. In-flight webs are left alone so a tap
        // can still land its hit (they self-expire after their short flight cap).
        boolean heldRecently = (level.getGameTime() - s.lastHeldTick) <= 10;
        if (!heldRecently && web != null && web.isStuck()) {
            discardActiveWeb(level, s);
            s.releasedInFlight = false;
            s.wasRoped = false;
            tickSpiderSense(player, level, s);
            return;
        }

        if (web != null && web.isStuck() && s.releasedInFlight) {
            // A tap: V was released while the web was still flying. Now that it has stuck,
            // cut it (the hit already applied on impact for mobs).
            discardActiveWeb(level, s);
            s.releasedInFlight = false;
        } else if (web != null && web.isStuck() && !web.isOnMob() && web.getStuckPos() != null) {
            // Roped to a block -> keep server-side rope/fall bookkeeping (swing is client-side).
            applySwing(player, web.getStuckPos(), s);
            ropedThisTick = true;
        }
        // (in-flight webs complete their arc on their own; mob latches persist for scroll-reel)
        s.wasRoped = ropedThisTick;

        // Auto-prime the punch-fling whenever a mob is latched and within range, so the player
        // can simply web a mob, walk up, and punch it — no scrolling required. Keeps the window
        // refreshed each tick the mob stays close.
        if (web != null && web.isStuck() && web.isOnMob()) {
            Entity tgt = level.getEntity(web.getStuckEntity());
            if (tgt != null && tgt.distanceTo(player) < 7.0) {
                s.fastReelUntil = level.getGameTime() + 10;
            }
        }

        // Spider-Sense passive: scan for danger and warn (tingle) on a throttle.
        tickSpiderSense(player, level, s);

        // CLIMBING TEMPORARILY DISABLED FOR TESTING.
        // The block-probe auto-cling approach has proven unreliable. Re-enable by
        // uncommenting below once a working approach is chosen (see notes on handleClimbing).
        // if (!ropedThisTick) {
        //     handleClimbing(player);
        // }
    }

    private void applySwing(ServerPlayer player, Vec3 anchor, SpiderState s) {
        // NOTE: the actual pendulum velocity is now applied CLIENT-SIDE (see
        // ClientForgeHandler#clientSpiderSwing) so the motion is smooth — server velocity
        // edits on the local player only sync intermittently and made the swing lurch once
        // a second. Here we only keep the server-side rope bookkeeping alive (initial rope
        // length + fall-distance reset) so scroll-reel state and fall-damage stay correct.
        Vec3 pos = player.position().add(0, player.getBbHeight() * 0.5, 0);
        double dist = anchor.subtract(pos).length();
        if (s.ropeLength < 0) s.ropeLength = dist;
        player.resetFallDistance();
    }

    // ===== Spider-Sense (passive) =====

    /**
     * Server-side danger scan. On a throttle, looks for incoming projectiles aimed at the
     * player, hostile mobs targeting the player, and primed creepers nearby. When a threat is
     * found, plays a subtle "tingle" cue (sound + a few particles around the head) and opens a
     * brief reaction window during which incoming damage is reduced (the dodge payoff).
     */
    private void tickSpiderSense(ServerPlayer player, ServerLevel level, SpiderState s) {
        if (!Config.spiderSenseEnabled) return;
        if (CompoundVEffect.arePowersSuppressed(player)) return;
        long now = level.getGameTime();
        if (now % Math.max(1, Config.spiderSenseScanInterval) != 0) return;
        if (now - s.lastSenseCue < Config.spiderSenseCooldownTicks) return;

        double r = Config.spiderSenseRadius;
        Vec3 ppos = player.position().add(0, player.getBbHeight() * 0.5, 0);
        var box = player.getBoundingBox().inflate(r);
        boolean threat = false;

        // (a) Incoming projectiles aimed at the player and closing in.
        if (Config.spiderSenseDetectProjectiles) {
            for (var proj : level.getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class, box)) {
                if (proj.getOwner() == player) continue; // ignore your own
                Vec3 v = proj.getDeltaMovement();
                if (v.lengthSqr() < 1.0e-4) continue;
                Vec3 toPlayer = ppos.subtract(proj.position());
                double dist = toPlayer.length();
                if (dist < 0.1) continue;
                // Velocity must point roughly at the player (closing) — dot of unit vectors.
                double aim = v.normalize().dot(toPlayer.normalize());
                if (aim > 0.9) { threat = true; break; }
            }
        }

        // (b) Hostile mobs that are targeting the player.
        if (!threat && Config.spiderSenseDetectAggro) {
            for (var mob : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, box)) {
                if (mob instanceof net.minecraft.world.entity.monster.Enemy && mob.getTarget() == player) {
                    threat = true; break;
                }
            }
        }

        // (c) Primed (fusing) creepers nearby.
        if (!threat && Config.spiderSenseDetectCreepers) {
            for (var creeper : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Creeper.class, box)) {
                if (creeper.getSwellDir() > 0) { threat = true; break; }
            }
        }

        if (!threat) return;

        // Warning cue: a clear chime + a ring of particles around the head (felt, not read).
        // No damage-reduction payoff — spider-sense is purely an awareness warning.
        s.lastSenseCue = now;
        // Play a clear, distinct chime. AMETHYST_BLOCK_CHIME is a plain SoundEvent (no holder
        // unwrap needed) and is reliably audible. Sent both directly to the player and into the
        // world so it's heard regardless of category volume settings.
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.5F);
        double hy = player.getY() + player.getEyeHeight() + 0.25;
        for (int i = 0; i < 6; i++) {
            double a = (Math.PI * 2 * i) / 6.0;
            level.sendParticles(WEB_PARTICLE,
                    player.getX() + Math.cos(a) * 0.5, hy, player.getZ() + Math.sin(a) * 0.5,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * Damage multiplier from the Spider-Sense reaction window (called by ForgeEvents). Returns
     * the configured reduced multiplier if the player is currently inside an active window,
     * otherwise 1.0 (no change).
     */
    public static float senseDamageMultiplier(LivingEntity entity) {
        // Spider-sense no longer grants damage reduction — it's a warning-only passive.
        return 1.0f;
    }

    private void handleClimbing(ServerPlayer player) {
        if (player.onGround() || player.isInWater() || player.getAbilities().flying) return;

        // Decide whether there's a climbable WALL beside the player (this only runs while
        // airborne — see the onGround early-return above).
        //
        // Cling when, for any horizontal direction:
        //   (a) the HEAD-level neighbour is solid  -> we're against the face of a wall
        //       (the normal mid-climb case), OR
        //   (b) the FOOT-level neighbour is solid AND the block BELOW that neighbour is
        //       also solid -> we're at the TOP EDGE of a wall that continues downward,
        //       so keep climbing up and over.
        //
        // What this deliberately EXCLUDES: a lone block one off the ground with air both
        // above it and below it (a floating/step obstacle). Its foot-neighbour is solid
        // but the block beneath is air, so (b) fails and (a) fails -> no cling. That kills
        // the counterintuitive friction without breaking wall tops.
        BlockPos base = player.blockPosition();
        Direction[] horiz = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        boolean wall = false;
        for (Direction d : horiz) {
            BlockPos head = base.above(1).relative(d); // head-level neighbour
            BlockPos foot = base.relative(d);          // foot-level neighbour
            BlockPos belowFoot = foot.below();         // block beneath the foot neighbour

            boolean headSolid = !player.level().getBlockState(head)
                    .getCollisionShape(player.level(), head).isEmpty();
            boolean footSolid = !player.level().getBlockState(foot)
                    .getCollisionShape(player.level(), foot).isEmpty();
            boolean belowFootSolid = !player.level().getBlockState(belowFoot)
                    .getCollisionShape(player.level(), belowFoot).isEmpty();

            if (headSolid || (footSolid && belowFootSolid)) {
                wall = true;
                break;
            }
        }
        if (!wall) return;

        Vec3 m = player.getDeltaMovement();
        double vy;
        if (player.isShiftKeyDown()) {
            vy = -0.15;                       // descend
        } else if (player.getXRot() < -15) {
            vy = 0.22;                        // look up to climb
        } else if (player.zza > 0) {
            vy = 0.18;                        // pressing forward into the wall also climbs
        } else {
            vy = 0.0;                         // cling in place (gravity cancelled)
        }
        // Dampen horizontal drift so you hug the wall, and assert vertical velocity.
        player.setDeltaMovement(m.x * 0.5, vy, m.z * 0.5);
        player.hurtMarked = true;
        player.resetFallDistance();
        player.fallDistance = 0;

        if (player.tickCount % 8 == 0 && player.level() instanceof ServerLevel sl) {
            sl.sendParticles(WEB_PARTICLE, player.getX(), player.getY() + 0.5, player.getZ(),
                    2, 0.2, 0.1, 0.2, 0.01);
        }
    }

    private static WebProjectileEntity activeWeb(ServerLevel level, SpiderState s) {
        if (s.activeWebId == -1) return null;
        Entity e = level.getEntity(s.activeWebId);
        if (e instanceof WebProjectileEntity web && web.isAlive()) return web;
        s.activeWebId = -1;
        return null;
    }

    private static void discardActiveWeb(ServerLevel level, SpiderState s) {
        if (s.activeWebId != -1) {
            Entity e = level.getEntity(s.activeWebId);
            if (e != null) e.discard();
            s.activeWebId = -1;
        }
        s.ropeLength = -1;
        s.wasRoped = false;
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof ServerPlayer player && entity.level() instanceof ServerLevel level) {
            SpiderState s = stateMap.get(player.getUUID());
            if (s != null) discardActiveWeb(level, s);
            stateMap.remove(player.getUUID());
        }
    }
}
