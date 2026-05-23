package blueduck.compound_v.effect;

import blueduck.compound_v.registry.EffectReg;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nullify — Experimental toggleable power.
 *
 * Press V to toggle. While active, creates a field (default 10 block radius) that
 * suppresses all Compound V powers for other entities within it.
 *
 * Affected entities receive the NULLIFIED marker effect (40 tick duration, refreshed every tick).
 * CompoundVEffect.arePowersSuppressed() checks for this marker, blocking:
 * - Active power usage (V key)
 * - Passive effect ticks (regen, speed, gaze, etc.)
 * - Combat stat bonuses (damage reduction, strength, knockback)
 * - Mob power AI
 *
 * The nullifier themselves is NOT affected by their own field.
 */
public class NullifyEffect extends CompoundVEffect {

    private static final Map<UUID, Boolean> activeState = new ConcurrentHashMap<>();
    private static final double NULLIFY_RADIUS = 10.0;
    private static final int EFFECT_REFRESH_DURATION = 40; // ticks — expires naturally when leaving field

    public NullifyEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }


    @Override
    public double getDamageReduction(int amplifier) {
        return super.getDamageReduction(amplifier) * 0.8;
    }

    public static boolean isActive(UUID uuid) {
        return activeState.getOrDefault(uuid, false);
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        UUID uuid = player.getUUID();
        boolean nowActive = !activeState.getOrDefault(uuid, false);
        activeState.put(uuid, nowActive);

        if (nowActive) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§c§lNullification Field: ACTIVE"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.5F);
        } else {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7Nullification Field: OFF"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 0.5F);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (!(entity instanceof ServerPlayer player)) return;
        if (!isActive(player.getUUID())) return;

        ServerLevel level = player.serverLevel();

        // Apply NULLIFIED marker to all nearby entities with Compound V effects
        AABB searchBox = player.getBoundingBox().inflate(NULLIFY_RADIUS);
        for (net.minecraft.world.entity.Entity e : level.getEntities(player, searchBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;

            // Only nullify entities that actually have Compound V powers
            boolean hasCompV = false;
            for (MobEffectInstance inst : target.getActiveEffects()) {
                if (inst.getEffect() instanceof CompoundVEffect) {
                    hasCompV = true;
                    break;
                }
            }
            if (!hasCompV) continue;

            // Refresh the NULLIFIED marker effect
            target.addEffect(new MobEffectInstance(EffectReg.NULLIFIED.get(),
                    EFFECT_REFRESH_DURATION, 0, false, true, true));
        }

        // Visual: particle ring on the ground every 10 ticks
        if (player.tickCount % 10 == 0) {
            for (int i = 0; i < 24; i++) {
                double angle = (2 * Math.PI * i) / 24.0;
                double px = player.getX() + Math.cos(angle) * NULLIFY_RADIUS;
                double pz = player.getZ() + Math.sin(angle) * NULLIFY_RADIUS;
                level.sendParticles(ParticleTypes.WITCH,
                        px, player.getY() + 0.1, pz,
                        1, 0, 0.1, 0, 0.01);
            }
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity,
                                          net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity != null) {
            activeState.remove(entity.getUUID());
        }
    }
}
