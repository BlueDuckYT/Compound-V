package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Telekinesis — hold V while looking at an entity to lift it; it floats toward a
 * point in front of your aim and tracks wherever you look. Release V to launch it
 * in your look direction. Works on mobs and players.
 *
 * Single source of truth is applyEffectTick (driven by a per-tick held flag), so a
 * grab/release can never double-fire — same edge-triggered model as Pyrokinesis.
 */
public class TelekinesisEffect extends CompoundVEffect {

    private static class TkState {
        UUID held;            // entity currently being telekinetically held
        long lastHeldTick = -100; // last tick a hold packet arrived (release = no packet for a few ticks)
        int holdTicks;        // how long the current grab has lasted
        long acquireCooldown; // brief delay before a new grab can be acquired after release
    }

    private static final Map<UUID, TkState> stateMap = new ConcurrentHashMap<>();
    // Per-player scroll-adjusted hold distance (falls back to config default when absent).
    private static final Map<UUID, Double> holdDistance = new ConcurrentHashMap<>();

    public TelekinesisEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    private static TkState state(UUID uuid) {
        return stateMap.computeIfAbsent(uuid, k -> new TkState());
    }

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        if (CompoundVEffect.arePowersSuppressed(player)) return;
        state(player.getUUID()).lastHeldTick = level.getGameTime();
    }

    /** Scroll-aware only while actually holding something, so normal scrolling is untouched. */
    @Override
    public boolean usesScroll(ServerPlayer player) {
        TkState s = stateMap.get(player.getUUID());
        return s != null && s.held != null;
    }

    /** Scroll adjusts how far in front the held entity floats, clamped to a sane range. */
    @Override
    public void scrollAdjust(ServerPlayer player, int amplifier, ServerLevel level, int dir) {
        UUID uuid = player.getUUID();
        double cur = holdDistance.getOrDefault(uuid, Config.telekinesisHoldDistance);
        double next = cur + dir; // 1 block per scroll notch
        double min = Config.telekinesisScrollMinDistance;
        double max = Config.telekinesisScrollMaxDistance;
        next = Math.max(min, Math.min(max, next));
        holdDistance.put(uuid, next);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) {
            if (entity instanceof Player p && entity.level() instanceof ServerLevel lvl) releaseAndRestore(lvl, p.getUUID());
            return;
        }
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID uuid = player.getUUID();
        TkState s = state(uuid);
        long now = level.getGameTime();
        // "Held" if a hold packet arrived within the last few ticks. This tolerates the
        // packet/tick ordering jitter that otherwise causes phantom releases (launch +
        // instant regrab). Release is only registered after a real gap with no packets.
        boolean held = (now - s.lastHeldTick) <= 3;

        if (held) {
            // Acquire a target on the first held tick if we don't have one.
            if (s.held == null && now >= s.acquireCooldown) {
                Entity target = raycastEntity(player, level, Config.telekinesisRange);
                if (target != null && canGrab(player, target)) {
                    s.held = target.getUUID();
                    s.holdTicks = 0;
                    holdDistance.put(player.getUUID(), Config.telekinesisHoldDistance);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 0.7F, 1.4F);
                }
            }
            // Steer the held entity toward the hold-point in front of the player's aim.
            if (s.held != null) {
                Entity e = level.getEntity(s.held);
                if (e == null || !e.isAlive() || e.distanceTo(player) > Config.telekinesisRange * 1.6) {
                    unhold(e);
                    s.held = null; // target gone/dead/too far
                } else if (e instanceof net.minecraft.world.entity.item.ItemEntity item) {
                    // Items are reeled straight to the player to be collected, not held aloft.
                    pullItemToPlayer(player, item, level);
                    if (!item.isAlive() || item.isRemoved()) s.held = null; // picked up
                    else s.holdTicks++;
                } else {
                    steerHeld(player, e, level);
                    s.holdTicks++;
                }
            }
        } else if (s.held != null) {
            // V released. Sneak-release = gentle drop (unharmed); otherwise launch —
            // but only if the held entity has actually been reeled in close. If it's
            // still far away (e.g. spam-tapping a distant target), it's just released
            // unharmed so the player can't repeatedly damage faraway mobs.
            Entity e = level.getEntity(s.held);
            if (e != null && e.isAlive()) {
                boolean inLaunchRange = e.distanceTo(player) <= Config.telekinesisLaunchRange;
                if (player.isShiftKeyDown() || !inLaunchRange) {
                    gentleDrop(e, level);
                } else {
                    launch(player, e, level);
                }
            }
            s.held = null;
            s.acquireCooldown = now + 4; // small debounce so a re-grab needs a fresh press
        }
    }

    /** Whether the player is allowed to grab this target. */
    private static boolean canGrab(ServerPlayer player, Entity target) {
        if (target == player) return false;
        if (target instanceof Player && !Config.telekinesisGrabsPlayers) return false;
        // Grab living entities, dropped items, and projectiles (to redirect them).
        return target instanceof LivingEntity
                || target instanceof net.minecraft.world.entity.item.ItemEntity
                || target instanceof net.minecraft.world.entity.projectile.Projectile;
    }


    /** Restores gravity and clears the held-stamp so the watchdog won't re-touch it. */
    private static void unhold(Entity e) {
        if (e == null) return;
        e.setNoGravity(false);
        e.getPersistentData().remove("CompoundVTKHeldUntil");
    }

    /** Floats the held entity toward a point in front of the player's eyes, tracking aim. */
    private void steerHeld(ServerPlayer player, Entity target, ServerLevel level) {
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 look = player.getLookAngle();
        // Hold-point ahead of the player at eye level (distance adjustable via scroll).
        double dist = holdDistance.getOrDefault(player.getUUID(), Config.telekinesisHoldDistance);
        Vec3 holdPoint = eye.add(look.scale(dist));

        Vec3 toPoint = holdPoint.subtract(target.position().add(0, target.getBbHeight() * 0.5, 0));
        // Spring-like steering: velocity proportional to offset, damped, capped.
        Vec3 desired = toPoint.scale(Config.telekinesisHoldStrength);
        double maxSpeed = 2.0;
        if (desired.length() > maxSpeed) desired = desired.normalize().scale(maxSpeed);

        target.setDeltaMovement(desired);
        target.hurtMarked = true;
        target.fallDistance = 0;
        target.hasImpulse = true;
        target.setNoGravity(true); // suspended; also avoids the server flight kick for players
        // Stamp the victim so it can restore its OWN gravity if the holder vanishes (portal,
        // disconnect, dimension change, death) and never gets to clear it. A watchdog in
        // ForgeEvents clears no-gravity on any entity whose stamp goes stale.
        target.getPersistentData().putLong("CompoundVTKHeldUntil", level.getGameTime() + 5);

        if (target.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    3, 0.3, 0.3, 0.3, 0.02);
        }
    }

    /** Launches the held entity in the player's look direction. */
    private void launch(ServerPlayer player, Entity target, ServerLevel level) {
        // Items are never thrown/attacked — telekinesis only ever pulls them in to collect or
        // drops them. If an item somehow reaches the launch path, drop it gently instead.
        if (target instanceof net.minecraft.world.entity.item.ItemEntity) {
            gentleDrop(target, level);
            return;
        }

        Vec3 look = player.getLookAngle();
        double force = Config.telekinesisLaunchForce;
        unhold(target);
        target.setDeltaMovement(look.scale(force).add(0, 0.2, 0));
        target.hurtMarked = true;
        target.fallDistance = 0;
        target.hasImpulse = true;

        if (target instanceof net.minecraft.world.entity.projectile.Projectile proj) {
            // Redirected projectile: the player now owns it (so it can hit the original
            // shooter) and is re-aimed along the look vector at full launch speed.
            proj.setOwner(player);
            proj.shoot(look.x, look.y, look.z, (float) Math.max(1.0, force), 0.2F);
        } else if (Config.telekinesisLaunchDamage > 0 && target instanceof LivingEntity living) {
            living.invulnerableTime = 0;
            living.hurt(player.damageSources().playerAttack(player), (float) Config.telekinesisLaunchDamage);
        }

        level.sendParticles(ParticleTypes.CLOUD,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                12, 0.2, 0.2, 0.2, 0.05);
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.7F);
    }

    /** Reels a dropped item straight toward the player and lets vanilla collect it on contact. */
    private void pullItemToPlayer(ServerPlayer player, net.minecraft.world.entity.item.ItemEntity item, ServerLevel level) {
        item.setNoGravity(true);
        item.setNoPickUpDelay(); // allow immediate vanilla pickup once it reaches the player
        Vec3 toPlayer = player.position().add(0, 0.4, 0).subtract(item.position());
        double dist = toPlayer.length();
        if (dist < 0.6) {
            // Close enough — hand it to vanilla pickup (handles full-inventory case correctly).
            item.playerTouch(player);
            return;
        }
        Vec3 vel = toPlayer.normalize().scale(Math.min(1.2, dist * Config.telekinesisHoldStrength + 0.3));
        item.setDeltaMovement(vel);
        item.hasImpulse = true;
        if (item.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    item.getX(), item.getY() + 0.2, item.getZ(), 2, 0.1, 0.1, 0.1, 0.0);
        }
    }

    /** Gently sets the held entity down: stops it and restores gravity, no launch, no damage. */
    private void gentleDrop(Entity target, ServerLevel level) {
        unhold(target);
        target.setDeltaMovement(0, -0.05, 0); // let it settle straight down
        target.fallDistance = 0;
        target.hurtMarked = true;
        level.sendParticles(ParticleTypes.ENCHANT,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                6, 0.2, 0.2, 0.2, 0.0);
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5F, 1.2F);
    }

    /** Restores gravity on a held entity and clears the grab (used on effect removal). */
    private static void releaseAndRestore(ServerLevel level, UUID uuid) {
        TkState s = stateMap.get(uuid);
        if (s == null || s.held == null) return;
        Entity e = level.getEntity(s.held);
        unhold(e);
        s.held = null;
    }

    /** Long-range raycast for a grabbable entity under the crosshair (living or dropped item). */
    private static Entity raycastEntity(ServerPlayer player, ServerLevel level, double range) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookDir = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookDir.scale(range));
        AABB searchBox = player.getBoundingBox().expandTowards(lookDir.scale(range)).inflate(2.0);
        Entity best = null;
        double closest = range + 1;
        for (Entity e : level.getEntities(player, searchBox,
                ent -> ent != player && ent.isAlive()
                        && (ent instanceof LivingEntity
                            || ent instanceof net.minecraft.world.entity.item.ItemEntity
                            || ent instanceof net.minecraft.world.entity.projectile.Projectile))) {
            AABB box = e.getBoundingBox().inflate(0.35);
            var hit = box.clip(eyePos, endPos);
            if (hit.isPresent()) {
                double dist = eyePos.distanceTo(hit.get());
                if (dist < closest) { closest = dist; best = e; }
            }
        }
        return best;
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity != null) {
            if (entity.level() instanceof ServerLevel lvl) releaseAndRestore(lvl, entity.getUUID());
            stateMap.remove(entity.getUUID());
            holdDistance.remove(entity.getUUID());
        }
    }
}
