package blueduck.compound_v.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * Luck - Passive power that increases all loot.
 * 3 levels via CompoundVEffectGiver(LUCK, 3).
 *
 * - Vanilla LUCK attribute (+1/+2/+3) - affects chest loot, fishing
 * - Looting boost (+1/+2/+3) via LootingLevelEvent in ForgeEvents
 *
 * No combat bonuses. (Block-fortune boost was removed.)
 */
public class LuckEffect extends CompoundVEffect {

    private static final UUID LUCK_MODIFIER_UUID = UUID.fromString("c3a7e1b9-4d5f-4e2a-8b1c-9f3d2e6a7c90");

    public LuckEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public double getStrengthMultiplier(int amplifier) {
        return 1.0; // no combat bonus
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;

        int level = amplifier + 1;
        var luckInstance = entity.getAttribute(Attributes.LUCK);
        if (luckInstance != null) {
            var existing = luckInstance.getModifier(LUCK_MODIFIER_UUID);
            if (existing == null) {
                luckInstance.addTransientModifier(new AttributeModifier(
                        LUCK_MODIFIER_UUID, "Compound V Luck",
                        level, AttributeModifier.Operation.ADDITION));
            } else if (existing.getAmount() != level) {
                luckInstance.removeModifier(LUCK_MODIFIER_UUID);
                luckInstance.addTransientModifier(new AttributeModifier(
                        LUCK_MODIFIER_UUID, "Compound V Luck",
                        level, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    /**
     * Get the luck level for ForgeEvents hooks (looting, bonus XP).
     */
    public static int getLuckLevel(LivingEntity entity) {
        var instance = entity.getEffect(
                blueduck.compound_v.registry.EffectReg.LUCK.get());
        if (instance == null) return 0;
        return instance.getAmplifier() + 1;
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return tick % 20 == 0;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity,
                                          net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        var luckInstance = entity.getAttribute(Attributes.LUCK);
        if (luckInstance != null) {
            luckInstance.removeModifier(LUCK_MODIFIER_UUID);
        }
    }
}
