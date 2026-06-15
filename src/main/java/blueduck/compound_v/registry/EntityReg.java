package blueduck.compound_v.registry;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.entity.WebProjectileEntity;
import blueduck.compound_v.entity.IceProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityReg {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CompoundVMod.MODID);

    public static final RegistryObject<EntityType<WebProjectileEntity>> WEB_PROJECTILE =
            ENTITIES.register("web_projectile", () ->
                    EntityType.Builder.<WebProjectileEntity>of(WebProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("web_projectile"));

    public static final RegistryObject<EntityType<IceProjectileEntity>> ICE_PROJECTILE =
            ENTITIES.register("ice_projectile", () ->
                    EntityType.Builder.<IceProjectileEntity>of(IceProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(6)
                            .updateInterval(4)
                            .build("ice_projectile"));
}
