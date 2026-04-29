package blueduck.compound_v.item;

import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.effect.negative.BadCompoundVEffect;
import blueduck.compound_v.util.PehkuiHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Anti-V — strips all Compound V effects from the drinker.
 *
 * - Removes all CompoundVEffect and BadCompoundVEffect instances
 * - Resets Pehkui scale as a safety net (in case Shrink/Enlarge were active)
 * - Can also be used on mobs via sneak+right-click (handled in ForgeEvents)
 */
public class AntiVItem extends Item {

    public AntiVItem(Properties props) {
        super(props);
    }

    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    public SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    public SoundEvent getEatingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide) {
            stripCompoundVEffects(entity);

            // Visual/audio feedback
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SMOKE,
                        entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                        20, 0.3, 0.5, 0.3, 0.05);
                sl.sendParticles(ParticleTypes.SQUID_INK,
                        entity.getX(), entity.getY() + 1.0, entity.getZ(),
                        5, 0.2, 0.3, 0.2, 0.02);
                sl.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.BEACON_DEACTIVATE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
            }
        }

        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return stack;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Strips all CompoundVEffect and BadCompoundVEffect instances from the entity.
     * Also resets Pehkui scale as a safety net.
     */
    public static void stripCompoundVEffects(LivingEntity entity) {
        // Collect effects to remove (can't modify during iteration)
        List<MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    || inst.getEffect() instanceof BadCompoundVEffect) {
                toRemove.add(inst.getEffect());
            }
        }

        for (MobEffect effect : toRemove) {
            entity.removeEffect(effect);
        }

        // Safety net: reset Pehkui scale in case Shrink/Enlarge was active
        if (ModList.get().isLoaded("pehkui")) {
            PehkuiHelper.resetScale(entity);
        }
    }
}
