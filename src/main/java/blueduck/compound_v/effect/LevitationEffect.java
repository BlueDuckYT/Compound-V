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

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity instanceof ServerPlayer && entity.hasEffect(MobEffects.LEVITATION)) {
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200, 4, false, false, false));
        }
    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);

        if (!player.hasEffect(MobEffects.LEVITATION)) {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200, 4, false, false, false));
        }
        else {
            player.removeEffect(MobEffects.LEVITATION);
        }

    }
}
