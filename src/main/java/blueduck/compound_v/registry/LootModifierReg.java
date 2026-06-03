package blueduck.compound_v.registry;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.loot.LuckFortuneModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LootModifierReg {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CompoundVMod.MODID);
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> LUCK_FORTUNE =
            LOOT_MODIFIERS.register("luck_fortune", LuckFortuneModifier.CODEC);
}
