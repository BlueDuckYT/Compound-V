package blueduck.compound_v.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ChargingEffect extends MobEffect {
    public ChargingEffect(MobEffectCategory category) {
        super(category, 1333402);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
    }
}
