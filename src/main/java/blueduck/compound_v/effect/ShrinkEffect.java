package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import blueduck.compound_v.util.PehkuiHelper;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.fml.ModList;
import org.joml.Vector3f;

/**
 * Shrink power using Pehkui integration.
 * Safely checks for Pehkui before calling any Pehkui classes.
 * The PehkuiHelper class is only loaded when Pehkui is confirmed present.
 */
public class ShrinkEffect extends CompoundVEffect {

    private static final DustParticleOptions SHRINK_PARTICLE = new DustParticleOptions(
            new Vector3f(0.3f, 1.0f, 0.3f), 0.5f);

    private boolean shrunk = false;

    public ShrinkEffect(MobEffectCategory category) {
        super(category);
    }

    public static boolean isPehkuiLoaded() {
        return ModList.get().isLoaded("pehkui");
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);

        if (!isPehkuiLoaded()) {
            // Shouldn't happen since we don't add to matrix without Pehkui, but safety check
            return;
        }

        // Toggle: check current scale to determine state
        float currentScale = PehkuiHelper.getTargetScale(player);
        boolean isCurrentlyShrunk = currentScale < 0.9f;

        if (isCurrentlyShrunk) {
            // Grow back to normal
            PehkuiHelper.resetScale(player);

            // Growth particles - expanding ring
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    15, 0.5, 0.5, 0.5, 0.1);
            level.sendParticles(SHRINK_PARTICLE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    20, 0.8, 1.0, 0.8, 0.05);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 0.6F, 0.8F);
        } else {
            // Shrink down
            PehkuiHelper.setScale(player, Config.shrinkScale);

            // Shrink particles - imploding ring
            level.sendParticles(SHRINK_PARTICLE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    25, 1.0, 1.0, 1.0, 0.02);
            level.sendParticles(ParticleTypes.POOF,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    10, 0.3, 0.3, 0.3, 0.02);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PISTON_CONTRACT, SoundSource.PLAYERS, 0.6F, 1.5F);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        // Subtle sparkle particles while shrunk
        if (isPehkuiLoaded() && entity.level() instanceof ServerLevel sl) {
            float scale = PehkuiHelper.getTargetScale(entity);
            if (scale < 0.9f) {
                sl.sendParticles(SHRINK_PARTICLE,
                        entity.getX(), entity.getY() + scale * 0.5, entity.getZ(),
                        1, scale * 0.2, scale * 0.2, scale * 0.2, 0.01);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return tick % 20 == 0;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        // Reset scale when effect is removed
        if (isPehkuiLoaded()) {
            PehkuiHelper.resetScale(entity);
        }
    }
}
