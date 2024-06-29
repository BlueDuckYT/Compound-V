package blueduck.compound_v.item;

import blueduck.compound_v.Config;
import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.registry.CompoundVEffectMatrix;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

        boolean isBad = !permanent ? Config.tempVBadOutcomes ? p_41349_.getRandom().nextDouble() < Config.badOutcomeChance : false : p_41349_.getRandom().nextDouble() < Config.badOutcomeChance;

        if (!p_41349_.isClientSide && !hasCompVAlready(p_41350_)) {
            if (isBad) {
                CompoundVEffectMatrix.FAILURE_MATRIX.get(p_41349_.getRandom().nextInt(CompoundVEffectMatrix.FAILURE_MATRIX.size())).apply(p_41350_, permanent);

            }
            else {
                CompoundVEffectMatrix.EFFECT_MATRIX.get(p_41349_.getRandom().nextInt(CompoundVEffectMatrix.EFFECT_MATRIX.size())).apply(p_41350_, permanent);
            }
        }

        if (p_41350_ instanceof Player && ((Player) p_41350_).getAbilities().instabuild) {
            return p_41348_;
        }

        return ItemStack.EMPTY;
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
