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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.fml.ModList;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * Shrink power using Pehkui integration.
 *
 * While shrunk (mini mushroom style):
 * - 300% speed boost via attribute modifier
 * - Higher jump (~3-4 blocks) via velocity boost on jump frame
 * - No fall damage (handled in ForgeEvents)
 * - Step height boost
 * - Water running (handled in ForgeEvents via PlayerTickEvent)
 */
public class ShrinkEffect extends CompoundVEffect {

    private static final DustParticleOptions SHRINK_PARTICLE = new DustParticleOptions(
            new Vector3f(0.3f, 1.0f, 0.3f), 0.5f);

    private static final UUID SHRINK_SPEED_UUID = UUID.fromString("a3c7e1b9-4d2f-4e8a-b6c5-9f1d3a7e2b84");
    private static final UUID SHRINK_STEP_UUID = UUID.fromString("a3c7e1b9-4d2f-4e8a-b6c5-9f1d3a7e2b85");
    private static final double SHRINK_SPEED_BONUS = 3.0; // 300% faster
    private static final double SHRINK_STEP_BONUS = 0.4;

    public ShrinkEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    public static boolean isPehkuiLoaded() {
        return ModList.get().isLoaded("pehkui");
    }

    public static boolean isShrunk(LivingEntity entity) {
        if (!isPehkuiLoaded()) return false;
        return PehkuiHelper.getTargetScale(entity) < 0.9f;
    }

    private static void applyBoosts(LivingEntity entity) {
        var speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(SHRINK_SPEED_UUID) == null) {
            speed.addTransientModifier(new AttributeModifier(
                    SHRINK_SPEED_UUID, "Shrink speed boost", SHRINK_SPEED_BONUS,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        var step = entity.getAttribute(net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (step != null && step.getModifier(SHRINK_STEP_UUID) == null) {
            step.addTransientModifier(new AttributeModifier(
                    SHRINK_STEP_UUID, "Shrink step height", SHRINK_STEP_BONUS,
                    AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeBoosts(LivingEntity entity) {
        var speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(SHRINK_SPEED_UUID);
        var step = entity.getAttribute(net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (step != null) step.removeModifier(SHRINK_STEP_UUID);
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        if (!isPehkuiLoaded()) return;

        float currentScale = PehkuiHelper.getTargetScale(player);
        boolean isCurrentlyShrunk = currentScale < 0.9f;

        if (isCurrentlyShrunk) {
            PehkuiHelper.resetScale(player);
            removeBoosts(player);

            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    15, 0.5, 0.5, 0.5, 0.1);
            level.sendParticles(SHRINK_PARTICLE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    20, 0.8, 1.0, 0.8, 0.05);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 0.6F, 0.8F);
        } else {
            PehkuiHelper.setScale(player, Config.shrinkScale);
            applyBoosts(player);

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
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!isPehkuiLoaded()) return;

        float scale = PehkuiHelper.getTargetScale(entity);

        if (scale < 0.9f) {
            // Attribute boosts — server-side only
            if (entity.level() instanceof ServerLevel sl) {
                applyBoosts(entity);

                // Sparkle particles
                if (entity.tickCount % 20 == 0) {
                    sl.sendParticles(SHRINK_PARTICLE,
                            entity.getX(), entity.getY() + scale * 0.5, entity.getZ(),
                            1, scale * 0.2, scale * 0.2, scale * 0.2, 0.01);
                }
            }
        } else {
            if (entity.level() instanceof ServerLevel) {
                removeBoosts(entity);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        removeBoosts(entity);
        if (isPehkuiLoaded()) {
            PehkuiHelper.resetScale(entity);
        }
    }
}
