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
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pyrokinesis — fire control.
 *
 * - Tap V: fire a fast blaze (small) fireball. Costs 1 charge.
 * - Hold V: charge up, then on release fire a fireball whose explosion power AND
 *   speed/size scale with how long it was charged. A full-charge release becomes a
 *   block-breaking LargeFireball. Same 1-charge cost — the cost is the wait.
 * - Sneak + V: a flame wave — a radial burst that ignites and damages nearby mobs
 *   with distance falloff and knockback.
 * - Passive: immunity to fire and lava (handled in ForgeEvents via {@link #hasFireImmunity}).
 *
 * Fireball capacity is intentionally NOT shown to the player — they learn it by feel.
 */
public class PyrokinesisEffect extends CompoundVEffect {

    private static class PyroState {
        int charges;            // current stored fireballs
        int regenCounter;       // ticks accumulated toward the next charge
        boolean charging;       // currently charging a shot
        int chargeTicks;        // how long the current shot has been charging
        boolean heldThisTick;   // set by holdActivate each tick V is held; cleared in applyEffectTick
        long flameWaveCooldownEnd;
        boolean initialized;
    }

    private static final Map<UUID, PyroState> stateMap = new ConcurrentHashMap<>();

    public PyrokinesisEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    private static PyroState state(UUID uuid) {
        return stateMap.computeIfAbsent(uuid, k -> {
            PyroState s = new PyroState();
            s.charges = Config.pyroMaxCharges;
            s.initialized = true;
            return s;
        });
    }

    /** Used by ForgeEvents to grant fire/lava immunity to Pyrokinesis users. */
    public static boolean hasFireImmunity(LivingEntity entity) {
        return entity.hasEffect(blueduck.compound_v.registry.EffectReg.PYROKINESIS.get())
                && !CompoundVEffect.arePowersSuppressed(entity);
    }

    // === Tap V ===

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        if (CompoundVEffect.arePowersSuppressed(player)) return;
        UUID uuid = player.getUUID();
        PyroState s = state(uuid);
        long now = level.getGameTime();

        if (player.isShiftKeyDown()) {
            // Sneak + V: flame wave (tap action)
            if (now < s.flameWaveCooldownEnd) return;
            s.flameWaveCooldownEnd = now + Config.pyroFlameWaveCooldown;
            flameWave(player, level);
            return;
        }
        // Standing fire is driven entirely by the held-flag state machine in
        // applyEffectTick (the press also produces a hold packet on the same tick),
        // so there is nothing to do here — this guarantees exactly one shot per press.
    }

    // === Hold V ===

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        if (CompoundVEffect.arePowersSuppressed(player)) return;
        if (player.isShiftKeyDown()) return; // sneak path is tap-only
        // Just record that V is held this tick. All charge/fire decisions happen in
        // applyEffectTick so firing is single-sourced and can't double up.
        state(player.getUUID()).heldThisTick = true;
    }

    // === Tick: regen + charge/release ===

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID uuid = player.getUUID();
        PyroState s = state(uuid);

        // Keep the player from burning (passive fire immunity safety).
        if (player.isOnFire()) player.clearFire();

        // Regenerate stored fireballs over time.
        if (s.charges < Config.pyroMaxCharges) {
            s.regenCounter++;
            if (s.regenCounter >= Config.pyroChargeRegenTicks) {
                s.regenCounter = 0;
                s.charges = Math.min(Config.pyroMaxCharges, s.charges + 1);
            }
        }

        // Charge state machine — single source of truth, driven by the held flag.
        boolean held = s.heldThisTick;
        s.heldThisTick = false; // consume for this tick

        // Charge-up disabled: V is a simple tap. Fire one basic fireball on the first held tick
        // of a press, then wait for release before allowing another.
        if (!Config.pyroChargeEnabled) {
            if (held) {
                if (!s.charging) {
                    s.charging = true; // reuse as a "fired this press" latch
                    s.chargeTicks = 0; // 0 charge => basic fireball
                    fireChargedFireball(player, level, s);
                }
            } else {
                s.charging = false; // released — ready for the next tap
            }
            return;
        }

        if (held) {
            if (!s.charging) {
                s.charging = true;
                s.chargeTicks = 0;
            } else {
                s.chargeTicks = Math.min(Config.pyroMaxChargeTime, s.chargeTicks + 1);
            }
            // Charge-up particles: out at arm's length, offset to the side/down so
            // they gather around the hand rather than over the camera.
            Vec3 look = player.getLookAngle();
            Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
            Vec3 hand = player.getEyePosition()
                    .add(look.scale(1.1))
                    .add(right.scale(0.35))
                    .add(0, -0.35, 0);
            int count = 1 + (int) (2 * ((float) s.chargeTicks / Config.pyroMaxChargeTime));
            level.sendParticles(ParticleTypes.FLAME, hand.x, hand.y, hand.z, count, 0.06, 0.06, 0.06, 0.005);
            if (s.chargeTicks == Config.pyroMaxChargeTime) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6F, 0.7F);
            }
        } else if (s.charging) {
            // V released this tick — fire exactly once, then return to idle.
            s.charging = false;
            fireChargedFireball(player, level, s);
            s.chargeTicks = 0;
        }
    }

    private void fireChargedFireball(ServerPlayer player, ServerLevel level, PyroState s) {
        if (s.charges <= 0) {
            player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.4F, 1.4F);
            return;
        }
        s.charges--;

        float chargeFrac = Config.pyroMaxChargeTime <= 0 ? 0f
                : (float) s.chargeTicks / Config.pyroMaxChargeTime;
        chargeFrac = Math.max(0f, Math.min(1f, chargeFrac));

        double speed = lerp(Config.pyroMinSpeed, Config.pyroMaxSpeed, chargeFrac);
        float explosion = (float) lerp(Config.pyroMinExplosion, Config.pyroMaxExplosion, chargeFrac);

        Vec3 look = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(look.scale(1.0));

        // Below ~40% charge => small blaze fireball; above => large explosive fireball.
        if (chargeFrac < 0.4f) {
            SmallFireball fb = new SmallFireball(level, player,
                    look.x * speed, look.y * speed, look.z * speed);
            fb.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            level.addFreshEntity(fb);
        } else {
            boolean breaksBlocks = Config.pyroFireballBreaksBlocks;
            LargeFireball fb = new LargeFireball(level, player,
                    look.x * speed, look.y * speed, look.z * speed,
                    Math.max(1, Math.round(explosion)));
            fb.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            // Block-breaking is enforced in ForgeEvents.pyroFireballExplosion, which
            // strips affected blocks when pyroFireballBreaksBlocks is disabled.
            level.addFreshEntity(fb);
        }

        level.sendParticles(ParticleTypes.FLAME, spawnPos.x, spawnPos.y, spawnPos.z,
                12, 0.1, 0.1, 0.1, 0.05);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F,
                0.8F + chargeFrac * 0.4F);
    }

    private void flameWave(ServerPlayer player, ServerLevel level) {
        double radius = Config.pyroFlameWaveRadius;
        float peak = (float) Config.pyroFlameWaveDamage;
        int fireSeconds = Config.pyroFlameWaveFireSeconds;

        // Visuals — spread thinly across the radius near ground level (not a dense
        // cloud on the camera). A ring of flame reads as a wave without whiteout.
        int ringPoints = 28;
        for (int i = 0; i < ringPoints; i++) {
            double ang = (2 * Math.PI * i) / ringPoints;
            double rr = radius * (0.5 + player.getRandom().nextDouble() * 0.5);
            double px = player.getX() + Math.cos(ang) * rr;
            double pz = player.getZ() + Math.sin(ang) * rr;
            level.sendParticles(ParticleTypes.FLAME, px, player.getY() + 0.3, pz, 1, 0.05, 0.15, 0.05, 0.02);
        }
        level.sendParticles(ParticleTypes.LAVA,
                player.getX(), player.getY() + 0.2, player.getZ(),
                8, radius * 0.35, 0.1, radius * 0.35, 0.0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 2.0F, 0.6F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.4F, 0.5F);

        AABB box = player.getBoundingBox().inflate(radius);
        for (Entity e : level.getEntities(player, box,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.distanceTo(player);
            if (dist > radius) continue;
            float falloff = (float) (1.0 - dist / (radius + 0.5));

            target.setSecondsOnFire(fireSeconds);
            if (peak > 0) CompoundVEffect.powerHurt(target, player.damageSources().playerAttack(player), peak * falloff);

            Vec3 push = target.position().subtract(player.position()).normalize();
            double force = 0.8 * falloff;
            target.setDeltaMovement(target.getDeltaMovement().add(push.x * force, 0.35 * falloff, push.z * force));
            target.hurtMarked = true;
        }
    }

    private static double lerp(double a, double b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity != null) stateMap.remove(entity.getUUID());
    }
}
