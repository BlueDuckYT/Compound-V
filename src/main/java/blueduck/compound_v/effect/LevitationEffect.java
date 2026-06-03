package blueduck.compound_v.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class LevitationEffect extends CompoundVEffect {
    public LevitationEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (entity instanceof net.minecraft.server.level.ServerPlayer && entity.hasEffect(MobEffects.LEVITATION)) {
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200, 4, false, false, false));
        }
    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        if (player.hasEffect(MobEffects.LEVITATION)) {
            player.removeEffect(MobEffects.LEVITATION);
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200, 4, false, false, false));
        }
    }
}
