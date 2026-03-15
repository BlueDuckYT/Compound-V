package blueduck.compound_v.client;

import blueduck.compound_v.CompoundVMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.Map;

@Mod.EventBusSubscriber(modid = CompoundVMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LaserBeamRenderer {

    private static final float EYE_SPACING = 0.1f;
    private static final float EYE_Y_OFFSET = -0.04f;
    private static final float FORWARD_OFFSET = 0.35f;

    // Beam widths — inner core is tight and bright, outer glow is wide
    private static final float CORE_HALF = 0.04f;
    private static final float GLOW_HALF = 0.12f;
    private static final float OUTER_HALF = 0.2f;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            LaserClientData.tick();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Map<Integer, LaserClientData.LaserInfo> lasers = LaserClientData.getActiveLasers();
        if (lasers.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float partialTick = event.getPartialTick();
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());

        for (Map.Entry<Integer, LaserClientData.LaserInfo> entry : lasers.entrySet()) {
            Entity entity = mc.level.getEntity(entry.getKey());
            if (!(entity instanceof Player player)) continue;

            LaserClientData.LaserInfo info = entry.getValue();
            Vec3 hitPos = new Vec3(info.hitX, info.hitY, info.hitZ);

            // Always use the entity's eye position — works correctly in F5 and for other players
            Vec3 eyeCenter = player.getEyePosition(partialTick);
            Vec3 lookDir = player.getViewVector(partialTick);
            float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot());
            float yawRad = (float) Math.toRadians(yaw);

            Vec3 forward = lookDir.scale(FORWARD_OFFSET);
            double rightX = -Math.cos(yawRad);
            double rightZ = -Math.sin(yawRad);

            Vec3 leftEye = eyeCenter.add(
                    rightX * -EYE_SPACING + forward.x,
                    EYE_Y_OFFSET + forward.y,
                    rightZ * -EYE_SPACING + forward.z);
            Vec3 rightEye = eyeCenter.add(
                    rightX * EYE_SPACING + forward.x,
                    EYE_Y_OFFSET + forward.y,
                    rightZ * EYE_SPACING + forward.z);

            // --- Color selection ---
            // Core is near-white with a tint, glow is saturated color
            float wr, wg, wb;   // white-hot core
            float cr, cg, cb;   // mid glow
            float gr, gg, gb;   // outer glow

            if (info.advanced) {
                // Homelander red
                wr = 1.0f; wg = 0.85f; wb = 0.8f;     // white-pink hot core
                cr = 1.0f; cg = 0.15f; cb = 0.05f;     // bright red mid
                gr = 1.0f; gg = 0.05f; gb = 0.02f;     // deep red outer
            } else if (info.blueVariant) {
                // Blue
                wr = 0.85f; wg = 0.92f; wb = 1.0f;     // white-blue hot core
                cr = 0.2f;  cg = 0.5f;  cb = 1.0f;      // bright blue mid
                gr = 0.1f;  gg = 0.3f;  gb = 1.0f;      // deep blue outer
            } else {
                // Orange
                wr = 1.0f; wg = 0.93f; wb = 0.8f;      // white-warm hot core
                cr = 1.0f; cg = 0.6f;  cb = 0.1f;       // bright orange mid
                gr = 1.0f; gg = 0.35f; gb = 0.02f;      // deep orange outer
            }

            // Subtle flicker
            float time = (player.tickCount + partialTick) * 0.8f;
            float flicker = 0.92f + 0.08f * Mth.sin(time * 6.0f);
            float glowFlicker = 0.85f + 0.15f * Mth.sin(time * 3.5f + 1.0f);

            poseStack.pushPose();
            poseStack.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = poseStack.last().pose();

            // Each eye gets 3 layers: white-hot core, colored mid-glow, and wide outer glow

            // --- Left eye beam ---
            renderBeaconBeam(consumer, matrix, leftEye, hitPos, wr, wg, wb, 1.0f * flicker, CORE_HALF);
            renderBeaconBeam(consumer, matrix, leftEye, hitPos, cr, cg, cb, 0.7f * flicker, GLOW_HALF);
            renderBeaconBeam(consumer, matrix, leftEye, hitPos, gr, gg, gb, 0.2f * glowFlicker, OUTER_HALF);

            // --- Right eye beam ---
            renderBeaconBeam(consumer, matrix, rightEye, hitPos, wr, wg, wb, 1.0f * flicker, CORE_HALF);
            renderBeaconBeam(consumer, matrix, rightEye, hitPos, cr, cg, cb, 0.7f * flicker, GLOW_HALF);
            renderBeaconBeam(consumer, matrix, rightEye, hitPos, gr, gg, gb, 0.2f * glowFlicker, OUTER_HALF);

            poseStack.popPose();
        }

        bufferSource.endBatch(RenderType.lightning());
    }

    /**
     * Renders a beacon-style square tube beam from start to end.
     * Four quads forming the walls of a rectangular prism.
     */
    private static void renderBeaconBeam(VertexConsumer consumer, Matrix4f matrix,
                                         Vec3 start, Vec3 end,
                                         float r, float g, float b, float a, float halfWidth) {
        Vec3 dir = end.subtract(start);
        double length = dir.length();
        if (length < 0.01) return;
        dir = dir.normalize();

        Vec3 arbitrary = (Math.abs(dir.y) > 0.99) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 side1 = dir.cross(arbitrary).normalize().scale(halfWidth);
        Vec3 side2 = dir.cross(side1).normalize().scale(halfWidth);

        Vec3 s_pp = start.add(side1).add(side2);
        Vec3 s_pn = start.add(side1).subtract(side2);
        Vec3 s_nn = start.subtract(side1).subtract(side2);
        Vec3 s_np = start.subtract(side1).add(side2);

        Vec3 e_pp = end.add(side1).add(side2);
        Vec3 e_pn = end.add(side1).subtract(side2);
        Vec3 e_nn = end.subtract(side1).subtract(side2);
        Vec3 e_np = end.subtract(side1).add(side2);

        addQuad(consumer, matrix, s_pp, s_pn, e_pn, e_pp, r, g, b, a);
        addQuad(consumer, matrix, s_nn, s_np, e_np, e_nn, r, g, b, a);
        addQuad(consumer, matrix, s_np, s_pp, e_pp, e_np, r, g, b, a);
        addQuad(consumer, matrix, s_pn, s_nn, e_nn, e_pn, r, g, b, a);
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix,
                                Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                float r, float g, float bl, float alpha) {
        consumer.vertex(matrix, (float) a.x, (float) a.y, (float) a.z).color(r, g, bl, alpha).endVertex();
        consumer.vertex(matrix, (float) b.x, (float) b.y, (float) b.z).color(r, g, bl, alpha).endVertex();
        consumer.vertex(matrix, (float) c.x, (float) c.y, (float) c.z).color(r, g, bl, alpha).endVertex();
        consumer.vertex(matrix, (float) d.x, (float) d.y, (float) d.z).color(r, g, bl, alpha).endVertex();
    }
}
