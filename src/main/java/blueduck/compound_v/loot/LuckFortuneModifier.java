package blueduck.compound_v.loot;

import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.effect.LuckEffect;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import java.util.function.Supplier;

public class LuckFortuneModifier extends LootModifier {
    public static final Supplier<Codec<LuckFortuneModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, LuckFortuneModifier::new)));

    public LuckFortuneModifier(LootItemCondition[] conditions) { super(conditions); }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (blockState == null) return generatedLoot;
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Player player)) return generatedLoot;
        if (CompoundVEffect.arePowersSuppressed(player)) return generatedLoot;
        int luckLevel = LuckEffect.getLuckLevel(player);
        if (luckLevel <= 0) return generatedLoot;
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) return generatedLoot;
        if (tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool) > 0) return generatedLoot;
        net.minecraft.world.item.Item blockItem = blockState.getBlock().asItem();
        ObjectArrayList<ItemStack> bonus = new ObjectArrayList<>();
        for (ItemStack stack : generatedLoot) {
            if (!stack.is(blockItem) && !stack.isEmpty()) {
                int bonusCount = player.getRandom().nextInt(luckLevel + 1);
                if (bonusCount > 0) { ItemStack extra = stack.copy(); extra.setCount(bonusCount); bonus.add(extra); }
            }
        }
        generatedLoot.addAll(bonus);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() { return CODEC.get(); }
}
