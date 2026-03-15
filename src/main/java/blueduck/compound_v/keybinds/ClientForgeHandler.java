package blueduck.compound_v.keybinds;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.util.C2SHeldPowerPacket;
import blueduck.compound_v.util.C2SPushPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompoundVMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeHandler {
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        // BUG FIX: Only process on END phase to prevent double-firing per tick
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().screen != null) return; // Don't fire while in menus

        // Single press: toggle activation (consumeClick only fires once per press)
        if (KeyBinding.POWER_KEY.consumeClick()) {
            PacketHandler.sendToServer(new C2SPushPacket());
        }

        // Held key: continuous activation (for laser eyes etc.)
        if (KeyBinding.POWER_KEY.isDown()) {
            PacketHandler.sendToServer(new C2SHeldPowerPacket());
        }
    }
}
