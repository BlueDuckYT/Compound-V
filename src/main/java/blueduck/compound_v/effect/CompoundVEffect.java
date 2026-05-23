package blueduck.compound_v.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class CompoundVEffect extends MobEffect {

    /**
     * Classifies powers for the multi-power system.
     * PASSIVE — always-on, no V key interaction
     * ACTIVE  — V key triggers or toggles the power's main ability
     */
    public enum PowerType {
        PASSIVE,
        ACTIVE
    }

    /**
     * Power tier determines base combat stats (damage reduction, strength, knockback).
     * Each tier has configurable base + perLevel scaling.
     * S = top tier (Stormfront, Instakill), D = lowest (utility powers).
     */
    public enum PowerTier {
        S, A, B, C, D;

        /**
         * Get the stat values for this tier at a given amplifier.
         * Formula: base + (amplifier * perLevel), clamped to [0, ∞).
         */
        public double getDamageReduction(int amplifier) {
            double base = blueduck.compound_v.Config.getTierDamageReduction(this);
            double perLevel = blueduck.compound_v.Config.getTierDamageReductionPerLevel(this);
            return Math.max(0, base + amplifier * perLevel);
        }

        public double getStrengthMultiplier(int amplifier) {
            double base = blueduck.compound_v.Config.getTierStrengthMultiplier(this);
            double perLevel = blueduck.compound_v.Config.getTierStrengthPerLevel(this);
            return Math.max(0, base + amplifier * perLevel);
        }

        public double getKnockbackReduction(int amplifier) {
            double base = blueduck.compound_v.Config.getTierKnockbackReduction(this);
            double perLevel = blueduck.compound_v.Config.getTierKnockbackPerLevel(this);
            return Math.max(0, base + amplifier * perLevel);
        }
    }

    public CompoundVEffect(MobEffectCategory category) {
        super(category, 1333402);
    }

    /**
     * Returns this power's type for multi-power rolling.
     * Default is PASSIVE — active effects override this.
     */
    public PowerType getPowerType() {
        return PowerType.PASSIVE;
    }

    /**
     * Returns this power's tier for stat computation.
     * Looks up from per-power config, falls back to defaultPowerTier.
     * GenericEffect overrides this with its own level→tier mapping.
     */
    public PowerTier getPowerTier() {
        return blueduck.compound_v.Config.getEffectTier(this);
    }

    /**
     * Returns true if the entity's Compound V powers are currently suppressed.
     */
    public static boolean arePowersSuppressed(net.minecraft.world.entity.LivingEntity entity) {
        boolean isPlayer = entity instanceof net.minecraft.server.level.ServerPlayer;
        if (blueduck.compound_v.util.VirusHelper.hasVirus(entity, isPlayer)) return true;
        if (entity.hasEffect(blueduck.compound_v.registry.EffectReg.NULLIFIED.get())) return true;
        return false;
    }

    /**
     * Returns true if two powers are incompatible for player multi-power rolling.
     */
    public static boolean areIncompatible(net.minecraft.world.effect.MobEffect a, net.minecraft.world.effect.MobEffect b) {
        if (isFlightRedundancy(a, b)) return true;
        if (isFlightRedundancy(b, a)) return true;
        return false;
    }

    private static boolean isFlightRedundancy(net.minecraft.world.effect.MobEffect flight, net.minecraft.world.effect.MobEffect other) {
        if (flight != blueduck.compound_v.registry.EffectReg.CREATIVE_FLIGHT.get()) return false;
        return other == blueduck.compound_v.registry.EffectReg.LASER_EYES_ADVANCED.get()
                || other == blueduck.compound_v.registry.EffectReg.STORMFRONT.get();
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
    }

    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        int k = 100 >> p_19456_;
        if (k > 0) {
            return p_19455_ % k == 0;
        } else {
            return true;
        }
    }

    /**
     * Damage reduction multiplier. Lower = more tanky.
     * Computed from this power's tier + amplifier scaling.
     */
    public double getDamageReduction(int amplifier) {
        return getPowerTier().getDamageReduction(amplifier);
    }

    /**
     * Strength multiplier when dealing damage. Higher = more damage.
     * Computed from this power's tier + amplifier scaling.
     */
    public double getStrengthMultiplier(int amplifier) {
        return getPowerTier().getStrengthMultiplier(amplifier);
    }

    /**
     * Knockback reduction multiplier. Lower = less knockback taken.
     * Computed from this power's tier + amplifier scaling.
     */
    public double getKnockbackReduction(int amplifier) {
        return getPowerTier().getKnockbackReduction(amplifier);
    }

    /**
     * Called when the player presses the power key (single press toggle).
     */
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
    }

    /**
     * Called every tick while the player holds the power key.
     */
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
    }
}
