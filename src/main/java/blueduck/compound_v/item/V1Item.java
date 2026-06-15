package blueduck.compound_v.item;

import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.effect.negative.BadCompoundVEffect;
import blueduck.compound_v.registry.CompoundVEffectMatrix;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * V1 — the original Compound V formula.
 *
 * - Always gives a good power (never a bad outcome)
 * - Always at maximum amplifier level
 * - Permanent
 * - Enchant glint to signal rarity
 */
public class V1Item extends Item {

    public V1Item(Properties props) {
        super(props);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // enchant glint
    }

    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    public SoundEvent getDrinkingSound() {
        return SoundEvents.WITCH_DRINK;
    }

    public SoundEvent getEatingSound() {
        return SoundEvents.WITCH_DRINK;
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide) {
            if (hasCompVAlready(entity)) {
                // Already has a power — upgrade it to max level
                upgradeToMaxLevel(entity);
            } else if (!CompoundVEffectMatrix.V1_EFFECT_MATRIX.isEmpty()) {
                CompoundVEffectMatrix.V1_EFFECT_MATRIX.get(
                        level.getRandom().nextInt(CompoundVEffectMatrix.V1_EFFECT_MATRIX.size())
                ).applyMaxLevel(entity);
            }
        }

        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return stack;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Upgrade all existing CompoundVEffect instances to their max amplifier level
     * and make them permanent. Looks up max level from all effect matrices.
     */
    private void upgradeToMaxLevel(LivingEntity entity) {
        boolean hasMimic = entity.hasEffect(blueduck.compound_v.registry.EffectReg.MIMIC.get());
        java.util.List<MobEffectInstance> toUpgrade = new java.util.ArrayList<>();
        boolean hasPowerAbsorption = false;
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && !(inst.getEffect() instanceof BadCompoundVEffect)) {
                // Skip Mimic-copied powers: a non-Mimic CompoundV effect on a Mimic
                // holder was copied and must not be upgraded or made permanent.
                if (hasMimic && !(inst.getEffect() instanceof blueduck.compound_v.effect.MimicEffect)) {
                    continue;
                }
                if (inst.getEffect() == blueduck.compound_v.registry.EffectReg.POWER_ABSORPTION.get()) {
                    hasPowerAbsorption = true;
                } else {
                    toUpgrade.add(inst);
                }
            }
        }
        if (hasPowerAbsorption) {
            entity.removeEffect(blueduck.compound_v.registry.EffectReg.POWER_ABSORPTION.get());
            blueduck.compound_v.effect.PowerAbsorptionEffect.clearCharge(entity.getUUID());
            int stormfrontMax = CompoundVEffectMatrix.getMaxLevel(blueduck.compound_v.registry.EffectReg.STORMFRONT.get());
            if (stormfrontMax < 0) stormfrontMax = 0;
            MobEffectInstance stormfront = new MobEffectInstance(
                    blueduck.compound_v.registry.EffectReg.STORMFRONT.get(),
                    MobEffectInstance.INFINITE_DURATION, stormfrontMax, false, false, false);
            stormfront.setCurativeItems(new java.util.ArrayList<>());
            entity.addEffect(stormfront);
        }
        for (MobEffectInstance old : toUpgrade) {
            int maxLevel = CompoundVEffectMatrix.getMaxLevel(old.getEffect());
            if (maxLevel < 0) maxLevel = old.getAmplifier();
            int target = maxLevel;
            // Overcharge: if enabled, a multi-level power (maxLevel >= 1) that is already
            // sitting at its max gets pushed one level beyond. Single-level powers
            // (maxLevel == 0) and unlisted powers are never overcharged.
            if (blueduck.compound_v.Config.v1LevelUpMaxed && maxLevel >= 1 && old.getAmplifier() >= maxLevel) {
                target = maxLevel + 1;
            }
            entity.removeEffect(old.getEffect());
            MobEffectInstance upgraded = new MobEffectInstance(old.getEffect(),
                    MobEffectInstance.INFINITE_DURATION, target, false, false, false);
            upgraded.setCurativeItems(new java.util.ArrayList<>());
            entity.addEffect(upgraded);
        }
    }

    private boolean hasCompVAlready(LivingEntity entity) {
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    || inst.getEffect() instanceof BadCompoundVEffect) {
                return true;
            }
        }
        return false;
    }
}
