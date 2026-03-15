package blueduck.compound_v.client;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.registry.EffectReg;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompoundVMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DeepFogHandler {

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.isUnderWater()) return;
        if (!mc.player.hasEffect(EffectReg.DEEP.get())) return;

        float starBrightness = mc.level.getStarBrightness((float) event.getPartialTick());
        float skyFactor = 1.0f - starBrightness;

        float colorBoost = 0.4f + 0.6f * skyFactor;
        event.setRed(Math.min(1.0f, event.getRed() + (Math.min(1.0f, event.getRed() * 4.0f) - event.getRed()) * colorBoost));
        event.setGreen(Math.min(1.0f, event.getGreen() + (Math.min(1.0f, event.getGreen() * 4.0f) - event.getGreen()) * colorBoost));
        event.setBlue(Math.min(1.0f, event.getBlue() + (Math.min(1.0f, event.getBlue() * 4.0f) - event.getBlue()) * colorBoost));
    }

    @SubscribeEvent
    public static void onFogDensity(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.isUnderWater()) return;
        if (!mc.player.hasEffect(EffectReg.DEEP.get())) return;

        float starBrightness = mc.level.getStarBrightness((float) event.getPartialTick());
        float skyFactor = 1.0f - starBrightness;

        float dayFar = 192f;
        float nightFar = 64f;
        float far = nightFar + (dayFar - nightFar) * skyFactor;

        float dayNear = -8f;
        float nightNear = 4f;
        float near = nightNear + (dayNear - nightNear) * skyFactor;

        event.setNearPlaneDistance(near);
        event.setFarPlaneDistance(far);
        event.setCanceled(true);
    }
}
