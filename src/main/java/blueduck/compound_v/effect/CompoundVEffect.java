package blueduck.compound_v.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class CompoundVEffect extends MobEffect {
    public enum PowerType { PASSIVE, ACTIVE }

    public enum PowerTier {
        SP, S, A, B, C, D;
        public double getDamageReduction(int amplifier) {
            return Math.max(0, blueduck.compound_v.Config.getTierDamageReduction(this) + amplifier * blueduck.compound_v.Config.getTierDamageReductionPerLevel(this));
        }
        public double getStrengthMultiplier(int amplifier) {
            return Math.max(0, blueduck.compound_v.Config.getTierStrengthMultiplier(this) + amplifier * blueduck.compound_v.Config.getTierStrengthPerLevel(this));
        }
        public double getKnockbackReduction(int amplifier) {
            return Math.max(0, blueduck.compound_v.Config.getTierKnockbackReduction(this) + amplifier * blueduck.compound_v.Config.getTierKnockbackPerLevel(this));
        }
        public double getKnockbackDealtMultiplier(int amplifier) {
            return Math.max(0, blueduck.compound_v.Config.getTierKnockbackDealt(this) + amplifier * blueduck.compound_v.Config.getTierKnockbackDealtPerLevel(this));
        }
    }

    public CompoundVEffect(MobEffectCategory category) { super(category, 1333402); }
    public PowerType getPowerType() { return PowerType.PASSIVE; }
    public PowerTier getPowerTier() { return blueduck.compound_v.Config.getEffectTier(this); }

    public static boolean arePowersSuppressed(LivingEntity entity) {
        boolean isPlayer = entity instanceof ServerPlayer;
        if (blueduck.compound_v.util.VirusHelper.hasVirus(entity, isPlayer)) return true;
        if (entity.hasEffect(blueduck.compound_v.registry.EffectReg.NULLIFIED.get())) return true;
        return false;
    }

    /**
     * Stat suppression is narrower than power suppression. The NULLIFIED effect
     * disables active and passive *powers* (V-key abilities, flight, laser AI, etc.)
     * but deliberately leaves the holder's passive *stat boosts* — strength,
     * damage reduction, knockback — intact. Only the virus fully suppresses stats.
     */
    public static boolean areStatsSuppressed(LivingEntity entity) {
        boolean isPlayer = entity instanceof ServerPlayer;
        return blueduck.compound_v.util.VirusHelper.hasVirus(entity, isPlayer);
    }

    public static boolean areIncompatible(net.minecraft.world.effect.MobEffect a, net.minecraft.world.effect.MobEffect b) {
        if (isFlightRedundancy(a, b) || isFlightRedundancy(b, a)) return true;
        return false;
    }

    private static boolean isFlightRedundancy(net.minecraft.world.effect.MobEffect flight, net.minecraft.world.effect.MobEffect other) {
        if (flight != blueduck.compound_v.registry.EffectReg.CREATIVE_FLIGHT.get()) return false;
        return other == blueduck.compound_v.registry.EffectReg.LASER_EYES_ADVANCED.get()
                || other == blueduck.compound_v.registry.EffectReg.STORMFRONT.get();
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {}

    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        int k = 100 >> p_19456_;
        return k > 0 ? p_19455_ % k == 0 : true;
    }

    public double getDamageReduction(int amplifier) { return getPowerTier().getDamageReduction(amplifier); }
    public double getStrengthMultiplier(int amplifier) { return getPowerTier().getStrengthMultiplier(amplifier); }
    public double getKnockbackReduction(int amplifier) { return getPowerTier().getKnockbackReduction(amplifier); }
    public double getKnockbackDealtMultiplier(int amplifier) { return getPowerTier().getKnockbackDealtMultiplier(amplifier); }
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {}
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {}

    /** Called the instant the power key is released (key-up edge). Default: no-op. */
    public void onRelease(ServerPlayer player, int amplifier, ServerLevel level) {}

    /**
     * Whether this power responds to the scroll wheel (so the client knows to capture
     * scroll input instead of changing the hotbar). Default false.
     */
    public boolean usesScroll(ServerPlayer player) { return false; }

    /** Called when a scroll-aware power is active and the player scrolls. dir is +1 (up) or -1 (down). */
    public void scrollAdjust(ServerPlayer player, int amplifier, ServerLevel level, int dir) {}
}
