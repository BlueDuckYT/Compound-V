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
     * Power-to-power upgrade map for V1. Drinking V1 while holding a KEY power promotes it to the
     * VALUE power (granted at max level) instead of just maxing the key's own level; anything not
     * in the map is maxed in place. Combines config-defined paths with paths registered by addon
     * mods through the public API, merged here so an addon registering at any point before a V1 is
     * consumed is honored regardless of load order. Config entries take precedence on conflicts.
     */
    private static java.util.Map<net.minecraft.world.effect.MobEffect, net.minecraft.world.effect.MobEffect> upgradeMap() {
        java.util.Map<net.minecraft.world.effect.MobEffect, net.minecraft.world.effect.MobEffect> merged =
                new java.util.HashMap<>(blueduck.compound_v.api.CompoundVUpgrades.getRegisteredPaths());
        merged.putAll(blueduck.compound_v.Config.v1UpgradePaths);
        return merged;
    }

    private void upgradeToMaxLevel(LivingEntity entity) {
        boolean hasMimic = entity.hasEffect(blueduck.compound_v.registry.EffectReg.MIMIC.get());
        var upgrades = upgradeMap();

        java.util.List<MobEffectInstance> toUpgrade = new java.util.ArrayList<>();
        // Powers that should be PROMOTED to a different power. Collected first, applied after the
        // iteration so we don't mutate the effect list while looping it.
        java.util.List<MobEffectInstance> toPromote = new java.util.ArrayList<>();

        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && !(inst.getEffect() instanceof BadCompoundVEffect)) {
                // Skip Mimic-copied powers: a non-Mimic CompoundV effect on a Mimic
                // holder was copied and must not be upgraded or made permanent.
                if (hasMimic && !(inst.getEffect() instanceof blueduck.compound_v.effect.MimicEffect)) {
                    continue;
                }
                if (upgrades.containsKey(inst.getEffect())) {
                    toPromote.add(inst);
                } else {
                    toUpgrade.add(inst);
                }
            }
        }

        // Apply promotions: remove the lesser power, grant the greater one at max level.
        for (MobEffectInstance old : toPromote) {
            net.minecraft.world.effect.MobEffect from = old.getEffect();
            net.minecraft.world.effect.MobEffect to = upgrades.get(from);
            // Power Absorption needs its charge cleared when it's consumed by the upgrade.
            if (from == blueduck.compound_v.registry.EffectReg.POWER_ABSORPTION.get()) {
                blueduck.compound_v.effect.PowerAbsorptionEffect.clearCharge(entity.getUUID());
            }
            entity.removeEffect(from);
            // Don't stack a duplicate if the target power is somehow already present.
            if (entity.hasEffect(to)) continue;
            int max = CompoundVEffectMatrix.getMaxLevel(to);
            if (max < 0) max = 0;
            MobEffectInstance granted = new MobEffectInstance(
                    to, MobEffectInstance.INFINITE_DURATION, max, false, false, false);
            granted.setCurativeItems(new java.util.ArrayList<>());
            entity.addEffect(granted);
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
