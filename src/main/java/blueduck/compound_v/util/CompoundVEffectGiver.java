package blueduck.compound_v.util;

import blueduck.compound_v.Config;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CompoundVEffectGiver {

    public MobEffect mobEffect;
    public int levels;

    public CompoundVEffectGiver(MobEffect effect, int maxLevel) {
        mobEffect = effect;
        levels = maxLevel - 1;
    }

    public void apply(LivingEntity entity, boolean permanent) {
        MobEffectInstance effect = new MobEffectInstance(mobEffect, permanent ? MobEffectInstance.INFINITE_DURATION : Config.tempVDuration, levels == 0 ? 0 : entity.getRandom().nextInt(levels), false, false, false);
        effect.setCurativeItems(new ArrayList<ItemStack>());
        entity.addEffect(effect);
    }

}
