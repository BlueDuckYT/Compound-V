package blueduck.compound_v.effect.negative;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Negative Compound V outcome: permanent blindness.
 *
 * Continuously reapplies the vanilla Blindness effect, making the player
 * essentially unable to see beyond a few blocks. A truly awful power roll.
 */
public class BlindnessEffect extends BadCompoundVEffect {
    public BlindnessEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false, false));
    }
}
