package blueduck.compound_v.item;

import blueduck.compound_v.Config;
import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.registry.CompoundVEffectMatrix;
import blueduck.compound_v.util.CompoundVEffectGiver;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class CompoundVItem extends Item {

    boolean permanent;

    public CompoundVItem(Properties p_41383_, boolean isPermanent) {
        super(p_41383_);
        permanent = isPermanent;
    }

    public int getUseDuration(ItemStack p_41360_) {
        return 40;
    }

    public UseAnim getUseAnimation(ItemStack p_41358_) {
        return UseAnim.DRINK;
    }

    public SoundEvent getDrinkingSound() {
        return SoundEvents.WITCH_DRINK;
    }

    public SoundEvent getEatingSound() {
        return SoundEvents.WITCH_DRINK;
    }

    public ItemStack finishUsingItem(ItemStack p_41348_, Level p_41349_, LivingEntity p_41350_) {
        super.finishUsingItem(p_41348_, p_41349_, p_41350_);

        if (!p_41349_.isClientSide) {
            if (permanent && hasTempCompV(p_41350_)) {
                // Permanent V while having temp effect → upgrade to permanent
                upgradeTempToPermanent(p_41350_);
            } else if (!permanent && hasTempCompV(p_41350_)) {
                // Another Temp V while already on Temp V → refresh duration
                refreshTempDuration(p_41350_);
            } else if (!hasCompVAlready(p_41350_)) {
                boolean isBad = permanent
                        ? p_41349_.getRandom().nextDouble() < Config.badOutcomeChance
                        : p_41349_.getRandom().nextDouble() < Config.tempVBadOutcomeChance;

                if (isBad) {
                    CompoundVEffectMatrix.FAILURE_MATRIX.get(p_41349_.getRandom().nextInt(CompoundVEffectMatrix.FAILURE_MATRIX.size())).apply(p_41350_, permanent);
                } else {
                    // Determine how many powers to roll
                    boolean multiEnabled = permanent ? Config.enableMultiPowers : Config.tempVEnableMultiPowers;
                    int maxCount = permanent ? Config.multiPowerMaxCount : Config.tempVMultiPowerMaxCount;
                    int powerCount = multiEnabled ? 1 + p_41349_.getRandom().nextInt(maxCount) : 1;

                    rollDistinctPowers(p_41350_, p_41349_, powerCount, permanent);
                }
            }
        }

        if (p_41350_ instanceof Player && ((Player) p_41350_).getAbilities().instabuild) {
            return p_41348_;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Roll N distinct powers from the effect matrix. Avoids duplicate effect types.
     * If only 1 power is rolled, it can be any type (passive or active).
     * When rolling 2+, only one active is normally allowed. Additional actives have a
     * 10% chance per roll — most multi-power combos are 1 active + N passives.
     * Mimic is always exclusive — if rolled, it's the only power granted.
     */
    private void rollDistinctPowers(LivingEntity entity, Level level, int count, boolean permanent) {
        java.util.ArrayList<CompoundVEffectGiver> pool = CompoundVEffectMatrix.EFFECT_MATRIX;
        if (pool.isEmpty()) return;

        java.util.Set<net.minecraft.world.effect.MobEffect> rolledEffects = new java.util.HashSet<>();
        java.util.List<CompoundVEffectGiver> toApply = new java.util.ArrayList<>();
        boolean hasActive = false;
        int attempts = 0;
        int maxAttempts = count * 30; // safety valve

        while (rolledEffects.size() < count && attempts < maxAttempts) {
            attempts++;
            CompoundVEffectGiver giver = pool.get(level.getRandom().nextInt(pool.size()));
            if (rolledEffects.contains(giver.mobEffect)) continue;

            // For multi-power: only one active allowed (single keybind)
            if (count > 1 && hasActive
                    && giver.mobEffect instanceof blueduck.compound_v.effect.CompoundVEffect cvEffect
                    && cvEffect.getPowerType() == blueduck.compound_v.effect.CompoundVEffect.PowerType.ACTIVE) {
                continue;
            }

            // Check incompatibilities with already-rolled powers
            boolean incompatible = false;
            for (CompoundVEffectGiver existing : toApply) {
                if (blueduck.compound_v.effect.CompoundVEffect.areIncompatible(giver.mobEffect, existing.mobEffect)) {
                    incompatible = true;
                    break;
                }
            }
            if (incompatible) continue;

            rolledEffects.add(giver.mobEffect);
            toApply.add(giver);

            if (giver.mobEffect instanceof blueduck.compound_v.effect.CompoundVEffect cvEffect
                    && cvEffect.getPowerType() == blueduck.compound_v.effect.CompoundVEffect.PowerType.ACTIVE) {
                hasActive = true;
            }

            // Mimic is exclusive — if we rolled it, stop and only give Mimic
            if (giver.mobEffect == blueduck.compound_v.registry.EffectReg.MIMIC.get()) {
                toApply.clear();
                toApply.add(giver);
                break;
            }
        }

        for (CompoundVEffectGiver giver : toApply) {
            giver.apply(entity, permanent);
        }
    }

    /**
     * Checks if the entity has any temp (non-infinite) CompoundVEffect (including bad outcomes).
     */
    private boolean hasTempCompV(LivingEntity entity) {
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && !inst.isInfiniteDuration()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Upgrades all temp CompoundV effects to permanent (infinite duration),
     * keeping the same effect and amplifier. Includes bad effects.
     */
    private void upgradeTempToPermanent(LivingEntity entity) {
        java.util.List<MobEffectInstance> toUpgrade = new java.util.ArrayList<>();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && !inst.isInfiniteDuration()) {
                toUpgrade.add(inst);
            }
        }
        for (MobEffectInstance old : toUpgrade) {
            entity.removeEffect(old.getEffect());
            MobEffectInstance upgraded = new MobEffectInstance(old.getEffect(),
                    MobEffectInstance.INFINITE_DURATION, old.getAmplifier(), false, false, false);
            upgraded.setCurativeItems(new java.util.ArrayList<>());
            entity.addEffect(upgraded);
        }
    }

    /**
     * Refreshes all temp CompoundV effects back to full duration.
     * Same effect, same amplifier, just resets the timer.
     */
    private void refreshTempDuration(LivingEntity entity) {
        java.util.List<MobEffectInstance> toRefresh = new java.util.ArrayList<>();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && !inst.isInfiniteDuration()) {
                toRefresh.add(inst);
            }
        }
        for (MobEffectInstance old : toRefresh) {
            entity.removeEffect(old.getEffect());
            MobEffectInstance refreshed = new MobEffectInstance(old.getEffect(),
                    Config.tempVDuration, old.getAmplifier(), false, false, false);
            refreshed.setCurativeItems(new java.util.ArrayList<>());
            entity.addEffect(refreshed);
        }
    }

    public boolean hasCompVAlready(LivingEntity entity) {
        if (!entity.getActiveEffects().isEmpty()) {
            for (int i = 0; i < entity.getActiveEffects().size(); i++) {
                if (((MobEffectInstance) entity.getActiveEffects().toArray()[i]).getEffect() instanceof CompoundVEffect) {
                    return true;
                }
            }
        }
        return false;
    }
}
