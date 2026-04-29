package blueduck.compound_v.registry;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.item.CompoundVItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemReg {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CompoundVMod.MODID);

    public static final RegistryObject<Item> COMPOUND_V = ITEMS.register("compound_v", () -> new CompoundVItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build()).stacksTo(1).rarity(Rarity.RARE), true));
    public static final RegistryObject<Item> TEMP_V = ITEMS.register("temp_v", () -> new CompoundVItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build()).stacksTo(1).rarity(Rarity.RARE), false));
    public static final RegistryObject<Item> V1 = ITEMS.register("v1", () -> new blueduck.compound_v.item.V1Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build()).stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ANTI_V = ITEMS.register("anti_v", () -> new blueduck.compound_v.item.AntiVItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build()).stacksTo(1).rarity(Rarity.UNCOMMON)));
}
