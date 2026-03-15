package blueduck.compound_v.effect;

import blueduck.compound_v.util.PehkuiHelper;
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
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import java.util.UUID;

public class EnlargeEffect extends CompoundVEffect {

    private static final UUID ENLARGE_DAMAGE_UUID = UUID.fromString("d5e78c9a-1b3f-4a7e-9c2d-8f6b5a4e3d21");
    private static final float ENLARGE_SCALE = 3.0f;

    public EnlargeEffect(MobEffectCategory category) {
        super(category);
    }

    public static boolean isPehkuiLoaded() {
        return ModList.get().isLoaded("pehkui");
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        if (!isPehkuiLoaded()) return;

        float currentScale = PehkuiHelper.getTargetScale(player);
        boolean isEnlarged = currentScale > 1.5f;

        if (isEnlarged) {
            PehkuiHelper.resetScale(player);
            removeDamageBoost(player);

            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    15, 0.8, 1.0, 0.8, 0.05);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PISTON_CONTRACT, SoundSource.PLAYERS, 0.6F, 1.2F);
        } else {
            PehkuiHelper.setScale(player, ENLARGE_SCALE);
            applyDamageBoost(player);

            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    30, 1.5, 2.0, 1.5, 0.1);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    player.getX(), player.getY(), player.getZ(),
                    10, 1.0, 0.5, 1.0, 0.02);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 0.8F, 0.5F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.3F, 0.6F);
        }
    }

    private void applyDamageBoost(ServerPlayer player) {
        var attr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null && attr.getModifier(ENLARGE_DAMAGE_UUID) == null) {
            attr.addTransientModifier(new AttributeModifier(
                    ENLARGE_DAMAGE_UUID, "Enlarge damage boost", 4.0, AttributeModifier.Operation.ADDITION));
        }
    }

    private void removeDamageBoost(ServerPlayer player) {
        var attr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            attr.removeModifier(ENLARGE_DAMAGE_UUID);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (isPehkuiLoaded() && entity instanceof ServerPlayer player
                && entity.level() instanceof ServerLevel sl) {
            float scale = PehkuiHelper.getTargetScale(player);
            if (scale > 1.5f && player.onGround() && player.getDeltaMovement().horizontalDistance() > 0.05) {
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        player.getX(), player.getY(), player.getZ(),
                        1, 0.5, 0.1, 0.5, 0.005);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return tick % 10 == 0;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof ServerPlayer player) {
            if (isPehkuiLoaded()) {
                PehkuiHelper.resetScale(player);
            }
            removeDamageBoost(player);
        }
    }
}
