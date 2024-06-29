package blueduck.compound_v.effect;

import blueduck.compound_v.registry.EffectReg;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class SpeedsterEffect extends CompoundVEffect {
    public SpeedsterEffect(MobEffectCategory category) {
        super(category);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.hasEffect(MobEffects.MOVEMENT_SPEED) && entity.hasEffect(MobEffects.DIG_SPEED) || !(entity instanceof Player)) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, amplifier * 2));
            entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, amplifier));
        }
    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);

        if (!player.hasEffect(MobEffects.MOVEMENT_SPEED) || !player.hasEffect(MobEffects.DIG_SPEED)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, amplifier * 2));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, amplifier));
        }
        else {
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            player.removeEffect(MobEffects.DIG_SPEED);
        }

    }
}
