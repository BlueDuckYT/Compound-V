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


}
