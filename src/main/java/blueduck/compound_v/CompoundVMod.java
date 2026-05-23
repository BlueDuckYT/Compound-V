package blueduck.compound_v;

import blueduck.compound_v.client.DensityRenderLayer;
import blueduck.compound_v.keybinds.KeyBinding;
import blueduck.compound_v.keybinds.PacketHandler;
import blueduck.compound_v.registry.EffectReg;
import blueduck.compound_v.registry.EntityReg;
import blueduck.compound_v.registry.ItemReg;
import blueduck.compound_v.registry.LootModifierReg;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;

@Mod(CompoundVMod.MODID)
public class CompoundVMod {
    public static final String MODID = "compound_v";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CompoundVMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ItemReg.ITEMS.register(modEventBus);
        EffectReg.EFFECTS.register(modEventBus);
        EntityReg.ENTITIES.register(modEventBus);
        LootModifierReg.LOOT_MODIFIERS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        EffectReg.addEffectsToMatrix();
        event.enqueueWork(() -> {
            PacketHandler.register();
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ItemReg.COMPOUND_V.get());
            event.accept(ItemReg.TEMP_V.get());
            event.accept(ItemReg.V1.get());
            event.accept(ItemReg.ANTI_V.get());
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }

        @SubscribeEvent
        public static void onKeySetup(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.POWER_KEY);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntityReg.WEB_PROJECTILE.get(), ThrownItemRenderer::new);
        }

        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            for (String skin : event.getSkins()) {
                var renderer = event.getSkin(skin);
                if (renderer instanceof PlayerRenderer playerRenderer) {
                    playerRenderer.addLayer(new DensityRenderLayer(playerRenderer));
                    playerRenderer.addLayer(new blueduck.compound_v.client.StarPowerRenderLayer(playerRenderer));
                    playerRenderer.addLayer(new blueduck.compound_v.client.ForcefieldRenderLayer(playerRenderer));
                }
            }
        }
    }
}
