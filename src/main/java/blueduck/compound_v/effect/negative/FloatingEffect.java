package blueduck.compound_v.effect.negative;

import blueduck.compound_v.registry.EffectReg;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class FloatingEffect extends BadCompoundVEffect {
    public FloatingEffect(MobEffectCategory category) {
        super(category);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200, 2, false, false, false));
        if (entity.blockPosition().getY() > 1000) {
            entity.removeEffect(EffectReg.FLOATING.get());
        }
    }


}
