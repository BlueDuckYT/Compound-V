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
        // Alex's Caves irradiation weakens supes: while irradiated at/above the configured level,
        // a Compound V holder's powers are suppressed (kryptonite-style). Config-gated and only
        // when Alex's Caves is loaded.
        if (blueduck.compound_v.Config.irradiationWeakensSupes
                && net.minecraftforge.fml.ModList.get().isLoaded("alexscaves")) {
            net.minecraft.world.effect.MobEffect irradiated =
                    blueduck.compound_v.util.AlexsCavesCompat.getIrradiatedEffect();
            if (irradiated != null) {
                net.minecraft.world.effect.MobEffectInstance inst = entity.getEffect(irradiated);
                if (inst != null && inst.getAmplifier() >= blueduck.compound_v.Config.irradiationWeakenMinLevel) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Stat suppression is narrower than power suppression. The NULLIFIED effect
     * disables active and passive *powers* (V-key abilities, flight, laser AI, etc.)
     * but deliberately leaves the holder's passive *stat boosts* - strength,
     * damage reduction, knockback - intact. Only the virus fully suppresses stats.
     */
    public static boolean areStatsSuppressed(LivingEntity entity) {
        boolean isPlayer = entity instanceof ServerPlayer;
        return blueduck.compound_v.util.VirusHelper.hasVirus(entity, isPlayer);
    }

    /**
     * Marks damage that is dealt BY a Compound V power (laser eyes, chest blast, etc.) which uses
     * the player-attack damage source so armor applies and kills credit the player - but which must
     * NOT receive the melee Strength multiplier. Powers wrap their hurt() call in beginPowerDamage()
     * / endPowerDamage(); the strength scaling in ForgeEvents checks isPowerDamage() and skips.
     * The hurt() call (and the LivingHurtEvent it fires) is synchronous on the server thread, so a
     * ThreadLocal flag is safe and self-contained.
     */
    private static final ThreadLocal<Boolean> POWER_DAMAGE = ThreadLocal.withInitial(() -> false);
    public static void beginPowerDamage() { POWER_DAMAGE.set(true); }
    public static void endPowerDamage() { POWER_DAMAGE.set(false); }
    public static boolean isPowerDamage() { return POWER_DAMAGE.get(); }

    /**
     * Deal damage FROM a Compound V power, flagged so the melee Strength/Berserker scaling skips it.
     * Use this instead of a raw target.hurt(...) for any power that uses the player-attack source
     * (for armor + kill credit) but shouldn't be amplified as if it were a melee swing.
     */
    public static boolean powerHurt(net.minecraft.world.entity.LivingEntity target,
                                    net.minecraft.world.damagesource.DamageSource source, float amount) {
        beginPowerDamage();
        try {
            return target.hurt(source, amount);
        } finally {
            endPowerDamage();
        }
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

    /**
     * Called when this power is lost (its effect expires or is removed). Powers that apply a
     * SECONDARY toggleable effect (e.g. Slime's Jump Boost, Cryokinesis' frost aura, Density's
     * slowness) must override this to clean those up, so the secondary effect doesn't linger
     * after the power is gone. Default does nothing.
     */
    public void clearSecondaryEffects(LivingEntity entity) {}

    /**
     * Whether this power grants flight (creative-style mayfly). Default false. Flight-granting
     * powers override this to return true; the core flight-maintenance logic (per-tick, on
     * dimension change, and on persist-through-death respawn) grants/keeps mayfly for any holder
     * of a power whose canFly() is true. Addon mods only need to override this on their own flight
     * powers - no changes to the core mod are required for their flight to be maintained.
     */
    public boolean canFly() { return false; }
}
