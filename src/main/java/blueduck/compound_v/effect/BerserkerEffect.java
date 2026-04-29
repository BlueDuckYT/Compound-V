package blueduck.compound_v.effect;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

/**
 * Berserker power: the lower your health, the more damage you deal.
 *
 * Damage scaling is handled in ForgeEvents.entityHurtEvent — when the
 * attacker has this effect, outgoing damage is multiplied based on
 * missing health percentage.
 *
 * At full health:  1.0x damage (no bonus)
 * At half health:  ~2.0x damage
 * At 1 heart:      ~3.5x damage
 * At half a heart:  ~4.0x damage
 *
 * Visual feedback: red particle aura that intensifies as health drops.
 * Audio: heartbeat sound when below 30% health.
 *
 * Works for both players and mobs — mobs get the same scaling through
 * the same event handler.
 */
public class BerserkerEffect extends CompoundVEffect {

    private static final DustParticleOptions RAGE_RED = new DustParticleOptions(
            new Vector3f(0.9f, 0.1f, 0.05f), 1.2f);
    private static final DustParticleOptions RAGE_DARK = new DustParticleOptions(
            new Vector3f(0.6f, 0.0f, 0.0f), 0.8f);
    private static final DustParticleOptions RAGE_BRIGHT = new DustParticleOptions(
            new Vector3f(1.0f, 0.3f, 0.1f), 1.5f);

    public BerserkerEffect(MobEffectCategory category) {
        super(category);
    }

    /**
     * Returns the damage multiplier based on the entity's current health percentage.
     * Used by ForgeEvents to scale outgoing damage.
     */
    public static float getDamageMultiplier(LivingEntity entity) {
        float healthPercent = entity.getHealth() / entity.getMaxHealth();
        // Multiplier scales from 1.0 at full health to 4.0 at near-zero
        // Formula: 1 + 3 * (1 - healthPercent)^1.5 — curves upward aggressively at low health
        float missingPercent = 1.0f - healthPercent;
        return 1.0f + 3.0f * (float) Math.pow(missingPercent, 1.5);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (!(entity instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();

        float healthPercent = player.getHealth() / player.getMaxHealth();
        float missingPercent = 1.0f - healthPercent;

        // No visual effects when at full or near-full health
        if (missingPercent < 0.1f) return;

        // Red particle aura — intensity scales with missing health
        if (player.tickCount % 5 == 0) {
            int particleCount = 1 + (int) (missingPercent * 8);
            double spread = 0.2 + missingPercent * 0.3;
            level.sendParticles(RAGE_RED,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    particleCount, spread, 0.5, spread, 0.01);
        }

        // Dark swirling particles at half health and below
        if (missingPercent > 0.5f && player.tickCount % 8 == 0) {
            level.sendParticles(RAGE_DARK,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    2 + (int) (missingPercent * 4), 0.4, 0.6, 0.4, 0.02);
        }

        // Bright rage flares below 25% health
        if (missingPercent > 0.75f && player.tickCount % 4 == 0) {
            level.sendParticles(RAGE_BRIGHT,
                    player.getX(), player.getY() + 1.2, player.getZ(),
                    3, 0.15, 0.3, 0.15, 0.03);
            level.sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + 0.8, player.getZ(),
                    1, 0.2, 0.3, 0.2, 0.01);
        }

        // Heartbeat sound when below 30% — escalating tempo
        if (missingPercent > 0.7f) {
            int beatInterval = missingPercent > 0.9f ? 15 : 25;
            if (player.tickCount % beatInterval == 0) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS,
                        0.3f + missingPercent * 0.4f, 0.8f + missingPercent * 0.4f);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }
}
