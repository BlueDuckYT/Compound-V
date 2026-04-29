package blueduck.compound_v.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Powerplex-style power absorption.
 *
 * Taking damage charges a meter (0–100). Pressing V discharges it as an
 * electric burst that damages nearby mobs. Below 10% charge the discharge
 * fizzles. Above that, range (4–16 blocks), damage (2–20), and particle
 * count scale linearly. At high charge (>50%) the burst lingers, pulsing
 * follow-up damage ticks that decay over time.
 */
public class PowerAbsorptionEffect extends CompoundVEffect {

    public static final float MAX_CHARGE = 100.0f;
    private static final float FIZZLE_THRESHOLD = 0.10f;

    private static final float MIN_RANGE = 2.0f;
    private static final float MAX_RANGE = 8.0f;
    private static final float MIN_DAMAGE = 1.0f;
    private static final float MAX_DAMAGE = 10.0f;
    private static final int MIN_PARTICLES = 10;
    private static final int MAX_PARTICLES = 40;

    /** Server-side charge per player. */
    private static final Map<UUID, Float> chargeMap = new ConcurrentHashMap<>();

    /** Active lingering discharges. */
    private static final Map<UUID, LingeringDischarge> lingeringMap = new ConcurrentHashMap<>();

    public PowerAbsorptionEffect(MobEffectCategory category) {
        super(category);
    }

    // ---- Charge API (called from ForgeEvents) ----

    /**
     * Add charge from taking damage.
     * @param playerUUID the player's UUID
     * @param damageAmount raw damage taken
     */
    public static void addCharge(UUID playerUUID, float damageAmount) {
        float current = chargeMap.getOrDefault(playerUUID, 0.0f);
        // Charge gain = damage * 3 (so ~33 raw damage = full charge)
        float gain = damageAmount * 3.0f;
        chargeMap.put(playerUUID, Math.min(MAX_CHARGE, current + gain));
    }

    public static float getChargePercent(UUID playerUUID) {
        return chargeMap.getOrDefault(playerUUID, 0.0f) / MAX_CHARGE;
    }

    public static void clearCharge(UUID playerUUID) {
        chargeMap.remove(playerUUID);
        lingeringMap.remove(playerUUID);
    }

    // ---- Effect ticking ----

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        if (!(entity instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        UUID uuid = player.getUUID();

        // Ambient sparks that intensify with charge
        float percent = getChargePercent(uuid);
        if (percent > 0.05f) {
            int count = (int) (1 + percent * 5);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    count, 0.3, 0.5, 0.3, 0.02);
        }

        // Process lingering discharge pulses
        LingeringDischarge ld = lingeringMap.get(uuid);
        if (ld != null) {
            ld.ticksRemaining--;
            if (ld.ticksRemaining <= 0) {
                lingeringMap.remove(uuid);
            } else if (ld.ticksRemaining % ld.tickInterval == 0) {
                doElectricBurst(player, level, ld.range, ld.damagePerPulse,
                        Math.max(5, (int) (ld.initialParticles * 0.4f)), false);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        // Tick every 5 ticks for responsive lingering pulses and ambient sparks
        return tick % 5 == 0;
    }

    // ---- Activation (V key press) ----

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);

        UUID uuid = player.getUUID();
        float charge = chargeMap.getOrDefault(uuid, 0.0f);
        float percent = charge / MAX_CHARGE;

        // Drain all charge
        chargeMap.put(uuid, 0.0f);

        // Below threshold: fizzle
        if (percent < FIZZLE_THRESHOLD) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    3, 0.2, 0.3, 0.2, 0.01);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.PLAYERS, 0.3F, 2.0F);
            return;
        }

        // Remap [threshold..1] → [0..1]
        float scaled = (percent - FIZZLE_THRESHOLD) / (1.0f - FIZZLE_THRESHOLD);

        float range = MIN_RANGE + (MAX_RANGE - MIN_RANGE) * scaled;
        float damage = MIN_DAMAGE + (MAX_DAMAGE - MIN_DAMAGE) * scaled;
        int particleCount = (int) (MIN_PARTICLES + (MAX_PARTICLES - MIN_PARTICLES) * scaled);

        // Main burst
        doElectricBurst(player, level, range, damage, particleCount, true);

        // Lingering pulses at >50% charge
        if (percent > 0.50f) {
            float lingerPercent = (percent - 0.50f) / 0.50f; // 0..1 over the top half
            int lingerTicks = (int) (20 + 60 * lingerPercent); // 1–4 seconds
            int tickInterval = Math.max(5, (int) (15 - 10 * lingerPercent)); // pulse every 5–15 ticks
            float pulseDamage = damage * (0.15f + 0.15f * lingerPercent); // 15–30% of burst damage

            lingeringMap.put(uuid, new LingeringDischarge(
                    lingerTicks, tickInterval, pulseDamage, range, particleCount));
        }
    }

    // ---- Burst logic ----

    private void doElectricBurst(ServerPlayer player, ServerLevel level,
                                 float range, float damage, int particleCount, boolean playSound) {
        BlockPos blockpos = player.blockPosition();
        AABB aabb = (new AABB(blockpos)).inflate(range);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, aabb);

        for (LivingEntity target : nearbyEntities) {
            if (target == player) continue;
            if (!target.isAlive() || target.isRemoved()) continue;
            double dist = player.distanceTo(target);
            if (dist > range) continue;

            // Damage falls off: 100% at center, 50% at max range
            float falloff = 1.0f - (float) (dist / range) * 0.5f;
            target.hurt(player.damageSources().indirectMagic(player, player), damage * falloff);

            // Sparks on each hit entity
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    8, 0.3, 0.4, 0.3, 0.1);
        }

        // Central burst — electric sparks radiating outward
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY() + 1.0, player.getZ(),
                particleCount, range * 0.4, range * 0.3, range * 0.4, 0.15);

        // Flash at player
        level.sendParticles(ParticleTypes.FLASH,
                player.getX(), player.getY() + 1.0, player.getZ(),
                1, 0.0, 0.0, 0.0, 0.0);

        // Soul fire at feet
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 0.2, player.getZ(),
                (int) (particleCount * 0.3), range * 0.15, 0.1, range * 0.15, 0.05);

        if (playSound) {
            float volume = 0.5f + (particleCount / (float) MAX_PARTICLES) * 1.5f;
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, volume, 0.8F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, volume * 0.7f, 1.2F);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity,
                                         net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            clearCharge(player.getUUID());
        }
    }

    @Override
    public double getStrengthMultiplier(int amplifier) {
        return super.getStrengthMultiplier(amplifier) * 1.75; // Energy absorption amplifies melee output
    }

    @Override
    public double getDamageReduction(int amplifier) {
        return super.getDamageReduction(amplifier) * 0.8; // Absorbs some incoming damage as energy
    }

    // ---- Internal ----

    private static class LingeringDischarge {
        int ticksRemaining;
        final int tickInterval;
        final float damagePerPulse;
        final float range;
        final int initialParticles;

        LingeringDischarge(int ticks, int interval, float damage, float range, int particles) {
            this.ticksRemaining = ticks;
            this.tickInterval = interval;
            this.damagePerPulse = damage;
            this.range = range;
            this.initialParticles = particles;
        }
    }
}
