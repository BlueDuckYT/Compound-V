package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Lifesteal — passive. Disables the holder's natural (hunger-based) regeneration;
 * the only way to heal is by dealing MELEE damage, which restores a fraction of the
 * damage dealt. The heal fraction scales with the power's level.
 *
 * The actual hooks live in ForgeEvents:
 *  - LivingHealEvent: cancels natural regen for holders.
 *  - LivingDamageEvent (attacker side): heals the attacker on melee hits.
 * This class only defines the power and the level→fraction lookup.
 */
public class LifestealEffect extends CompoundVEffect {

    public LifestealEffect(MobEffectCategory category) {
        super(category);
    }

    // Passive: no V-key action.

    /** Heal fraction (of damage dealt) for a given amplifier level (0-indexed). */
    public static double getHealFraction(int amplifier) {
        double[] fracs = Config.lifestealLevelFractions;
        if (fracs == null || fracs.length == 0) return 0.2;
        int idx = Math.max(0, Math.min(amplifier, fracs.length - 1));
        return fracs[idx];
    }

    /** True if the entity should have natural regen suppressed. */
    public static boolean suppressesNaturalRegen(LivingEntity entity) {
        return entity.hasEffect(blueduck.compound_v.registry.EffectReg.LIFESTEAL.get())
                && !CompoundVEffect.arePowersSuppressed(entity);
    }
}
