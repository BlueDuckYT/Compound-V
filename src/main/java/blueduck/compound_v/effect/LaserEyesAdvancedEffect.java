package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.joml.Vector3f;

/**
 * Advanced (Homelander-level) laser eyes.
 * Red beams, higher damage, higher fire chance, and permanent creative flight.
 * Flight stays on — V press does NOT toggle it off.
 */
public class LaserEyesAdvancedEffect extends LaserEyesEffect {

    private static final DustParticleOptions RED_CORE = new DustParticleOptions(
            new Vector3f(1.0f, 0.1f, 0.05f), 1.4f);
    private static final DustParticleOptions RED_GLOW = new DustParticleOptions(
            new Vector3f(1.0f, 0.3f, 0.15f), 0.8f);

    public LaserEyesAdvancedEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    protected boolean isAdvanced() {
        return true;
    }

    @Override
    protected float getLaserDamage() {
        return (float) Config.laserAdvancedDamage;
    }

    @Override
    protected int getLaserRange() {
        return Config.laserAdvancedRange;
    }

    @Override
    protected double getFireChance() {
        return Config.laserAdvancedFireChance;
    }

    @Override
    protected DustParticleOptions getCoreParticle(int colorIndex) {
        return RED_CORE;
    }

    @Override
    protected DustParticleOptions getGlowParticle(int colorIndex) {
        return RED_GLOW;
    }

    @Override
    public double getStrengthMultiplier(int amplifier) {
        return super.getStrengthMultiplier(amplifier) * 2.0; // Double the damage boost
    }

    @Override
    public double getDamageReduction(int amplifier) {
        return super.getDamageReduction(amplifier) * 0.7; // 30% more damage reduction on top of base
    }

    @Override
    public double getKnockbackReduction(int amplifier) {
        return super.getKnockbackReduction(amplifier) * 0.5; // Half the knockback
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        // Keep flight always available
        if (entity instanceof ServerPlayer player) {
            if (!player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }

            // Menacing red glow particles around eyes while flying
            if (player.getAbilities().flying && entity.level() instanceof ServerLevel sl) {
                sl.sendParticles(RED_GLOW,
                        entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(),
                        2, 0.15, 0.05, 0.15, 0.01);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return tick % 10 == 0;
    }

    // No activate() override — V press does NOT toggle flight off.

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.holdActivate(player, amplifier, level);

        if (player.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + player.getEyeHeight(), player.getZ(),
                    2, 0.1, 0.05, 0.1, 0.02);
        }

        if (player.tickCount % 5 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_BURN, SoundSource.PLAYERS, 0.3F, 0.5F);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof ServerPlayer player) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }
}
