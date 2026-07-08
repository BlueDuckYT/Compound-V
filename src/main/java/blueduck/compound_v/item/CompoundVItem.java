package blueduck.compound_v.item;

import blueduck.compound_v.Config;
import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.effect.MimicEffect;
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
            } else if (permanent && Config.levelUpOnDrink && hasPermanentCompV(p_41350_)) {
                // Permanent V while already permanently powered → level up current effect(s) by 1
                levelUpCurrentEffects(p_41350_);
            } else if (!hasCompVAlready(p_41350_)) {
                boolean isBad = permanent
                        ? p_41349_.getRandom().nextDouble() < Config.badOutcomeChance
                        : p_41349_.getRandom().nextDouble() < Config.tempVBadOutcomeChance;

                if (isBad) {
                    CompoundVEffectMatrix.FAILURE_MATRIX.get(p_41349_.getRandom().nextInt(CompoundVEffectMatrix.FAILURE_MATRIX.size())).apply(p_41350_, permanent);
                } else {
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
     * A Mimic-copied power is a non-Mimic CompoundVEffect present on an entity that
     * also has the Mimic effect. Because Mimic is exclusive when injected (it clears
     * all other rolled powers), the only way a Mimic holder has another CompoundV
     * effect is that it was copied — and copies must stay temporary. Drinking
     * Compound V or Temp V must NOT make a copied power permanent or extend it.
     */
    private boolean isMimickedCopy(LivingEntity entity, MobEffectInstance inst) {
        if (inst.getEffect() instanceof MimicEffect) return false;
        return entity.hasEffect(blueduck.compound_v.registry.EffectReg.MIMIC.get());
    }

    /**
     * Checks if the entity has any temp (non-infinite) CompoundVEffect (including bad outcomes).
     */
    private boolean hasTempCompV(LivingEntity entity) {
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && !inst.isInfiniteDuration()
                    && !isMimickedCopy(entity, inst)) {
                return true;
            }
        }
        return false;
    }

    /** True if the entity has at least one permanent (infinite) CompoundV effect (excluding copies). */
    private boolean hasPermanentCompV(LivingEntity entity) {
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && inst.isInfiniteDuration()
                    && !isMimickedCopy(entity, inst)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Raises the amplifier of each permanent CompoundV effect by 1, capped at the
     * effect's max level. Mimic copies are skipped. Effects not present in any matrix
     * (max level unknown, returns -1) are left unchanged so SPECIAL-tier / unlisted
     * powers can never be pushed to an invalid level or crash.
     */
    private void levelUpCurrentEffects(LivingEntity entity) {
        java.util.List<MobEffectInstance> toLevel = new java.util.ArrayList<>();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && inst.isInfiniteDuration()
                    && !isMimickedCopy(entity, inst)) {
                toLevel.add(inst);
            }
        }
        for (MobEffectInstance old : toLevel) {
            int maxLevel;
            if (old.getEffect() instanceof blueduck.compound_v.effect.GenericEffect) {
                // Generic's amplifier IS the power tier (0=D … 4=S). Its level-up ceiling is the
                // top tier (amplifier 4), NOT the matrix roll-range value — that value only bounds
                // what tier Generic can ROLL at, and using it here would DOWNGRADE a high-tier
                // Generic (e.g. Generic V -> III) on drink. Cap at S tier instead.
                maxLevel = blueduck.compound_v.effect.GenericEffect.maxTierAmplifier();
            } else {
                maxLevel = CompoundVEffectMatrix.getMaxLevel(old.getEffect());
            }
            int current = old.getAmplifier();
            // Unknown max (-1) or already at/over max → leave as-is (no crash, no over-level).
            int target = (maxLevel >= 0) ? Math.min(current + 1, maxLevel) : current;
            if (target <= current) continue;
            entity.removeEffect(old.getEffect());
            MobEffectInstance leveled = new MobEffectInstance(old.getEffect(),
                    MobEffectInstance.INFINITE_DURATION, target, false, false, false);
            leveled.setCurativeItems(new java.util.ArrayList<>());
            entity.addEffect(leveled);
        }
    }

    /**
     * Upgrades all temp CompoundV effects to permanent (infinite duration),
     * keeping the same effect and amplifier. Includes bad effects.
     * Mimic-copied powers are intentionally excluded so they cannot be made permanent.
     */
    private void upgradeTempToPermanent(LivingEntity entity) {
        java.util.List<MobEffectInstance> toUpgrade = new java.util.ArrayList<>();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && !inst.isInfiniteDuration()
                    && !isMimickedCopy(entity, inst)) {
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
     * Mimic-copied powers are intentionally excluded so their duration cannot be extended.
     */
    private void refreshTempDuration(LivingEntity entity) {
        java.util.List<MobEffectInstance> toRefresh = new java.util.ArrayList<>();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && !inst.isInfiniteDuration()
                    && !isMimickedCopy(entity, inst)) {
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

    private void rollDistinctPowers(LivingEntity entity, Level level, int count, boolean permanent) {
        java.util.ArrayList<CompoundVEffectGiver> pool = CompoundVEffectMatrix.EFFECT_MATRIX;
        if (pool.isEmpty()) return;
        java.util.Set<net.minecraft.world.effect.MobEffect> rolledEffects = new java.util.HashSet<>();
        java.util.List<CompoundVEffectGiver> toApply = new java.util.ArrayList<>();
        boolean hasActive = false;
        int attempts = 0;
        int maxAttempts = count * 30;
        while (rolledEffects.size() < count && attempts < maxAttempts) {
            attempts++;
            CompoundVEffectGiver giver = pool.get(level.getRandom().nextInt(pool.size()));
            if (rolledEffects.contains(giver.mobEffect)) continue;
            if (count > 1 && hasActive
                    && giver.mobEffect instanceof CompoundVEffect cvEffect
                    && cvEffect.getPowerType() == CompoundVEffect.PowerType.ACTIVE) continue;
            boolean incompatible = false;
            for (CompoundVEffectGiver existing : toApply) {
                if (CompoundVEffect.areIncompatible(giver.mobEffect, existing.mobEffect)) { incompatible = true; break; }
            }
            if (incompatible) continue;
            rolledEffects.add(giver.mobEffect);
            toApply.add(giver);
            if (giver.mobEffect instanceof CompoundVEffect cvEffect
                    && cvEffect.getPowerType() == CompoundVEffect.PowerType.ACTIVE) hasActive = true;
            if (giver.mobEffect == blueduck.compound_v.registry.EffectReg.MIMIC.get()) {
                toApply.clear(); toApply.add(giver); break;
            }
        }
        for (CompoundVEffectGiver giver : toApply) giver.apply(entity, permanent);
    }
}
