package blueduck.compound_v.effect.negative;

import blueduck.compound_v.Config;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Wither — inflicts a high-amplifier Wither effect, refreshed continuously so
 * the victim is under sustained heavy decay for as long as they carry this power.
 */
public class WitherEffect extends BadCompoundVEffect {

    public WitherEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return tick % 20 == 0; // re-apply once a second
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        int amp = Math.max(0, Config.witherAmplifier);
        // Refresh a strong Wither so it never lapses while the power is held.
        entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, amp, false, true, true));
    }
}
