package blueduck.compound_v.effect;

import net.minecraft.world.effect.MobEffectCategory;

/**
 * Generic — The baseline super-soldier serum effect.
 * Each amplifier level corresponds to a power tier:
 *   Level 1 (amp 0) = Tier D, Level 2 = C, Level 3 = B, Level 4 = A, Level 5 = S
 */
public class GenericEffect extends CompoundVEffect {
    private static final PowerTier[] LEVEL_TO_TIER = {
            PowerTier.D, PowerTier.C, PowerTier.B, PowerTier.A, PowerTier.S
    };

    public GenericEffect(MobEffectCategory category) {
        super(category);
    }

    /** Highest valid amplifier for Generic (the top tier, S). Used as the level-up ceiling. */
    public static int maxTierAmplifier() {
        return LEVEL_TO_TIER.length - 1; // 4 = S tier
    }

    private static PowerTier tierForAmplifier(int amplifier) {
        if (amplifier < 0) return PowerTier.D;
        if (amplifier >= LEVEL_TO_TIER.length) return PowerTier.S;
        return LEVEL_TO_TIER[amplifier];
    }

    @Override
    public double getDamageReduction(int amplifier) {
        return tierForAmplifier(amplifier).getDamageReduction(0);
    }

    @Override
    public double getStrengthMultiplier(int amplifier) {
        return tierForAmplifier(amplifier).getStrengthMultiplier(0);
    }

    @Override
    public double getKnockbackReduction(int amplifier) {
        return tierForAmplifier(amplifier).getKnockbackReduction(0);
    }

    @Override
    public double getKnockbackDealtMultiplier(int amplifier) {
        return tierForAmplifier(amplifier).getKnockbackDealtMultiplier(0);
    }
}
