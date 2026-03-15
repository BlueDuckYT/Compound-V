package blueduck.compound_v.effect.negative;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class SlowEffect extends BadCompoundVEffect {
    public SlowEffect(MobEffectCategory category) {
        super(category);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, amplifier, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, amplifier, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false, false));
    }
}
