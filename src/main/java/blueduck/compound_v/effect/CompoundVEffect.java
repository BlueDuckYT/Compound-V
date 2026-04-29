package blueduck.compound_v.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class CompoundVEffect extends MobEffect {
    public CompoundVEffect(MobEffectCategory category) {
        super(category, 1333402);
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
     * Damage reduction multiplier. Lower = more tanky. Default uses config value.
     * Scales with amplifier: higher levels = tankier.
     * Subclasses can override for custom scaling.
     */
    public double getDamageReduction(int amplifier) {
        int level = amplifier + 1;
        return blueduck.compound_v.Config.damageReduction / level;
    }

    /**
     * Strength multiplier when dealing damage. Higher = more damage. Default uses config value.
     * Scales with amplifier: higher levels = stronger.
     * Subclasses can override for custom scaling.
     */
    public double getStrengthMultiplier(int amplifier) {
        int level = amplifier + 1;
        return blueduck.compound_v.Config.strengthMultiplier * level;
    }

    /**
     * Knockback reduction multiplier. Lower = less knockback taken. Default uses config value.
     * Scales with amplifier: higher levels = less knockback.
     * Subclasses can override for custom scaling.
     */
    public double getKnockbackReduction(int amplifier) {
        int level = amplifier + 1;
        return blueduck.compound_v.Config.knockbackReduction / level;
    }

    /**
     * Called when the player presses the power key (single press toggle).
     */
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
    }

    /**
     * Called every tick while the player holds the power key.
     * Override this for continuous effects like laser eyes.
     */
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
    }
}
