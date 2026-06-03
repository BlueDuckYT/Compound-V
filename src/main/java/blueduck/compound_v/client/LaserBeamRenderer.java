package blueduck.compound_v.client;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.util.S2CLaserSyncPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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

    // Player eye offsets
    private static final float EYE_SPACING = 0.1f;
    private static final float EYE_Y_OFFSET = -0.04f;
    private static final float FORWARD_OFFSET = 0.35f;

    // Beam widths — inner core is tight and bright, outer glow is wide
    private static final float CORE_HALF = 0.04f;
    private static final float GLOW_HALF = 0.12f;
    private static final float OUTER_HALF = 0.2f;

    // --- Color palettes per colorIndex ---
    // Each row: { white-hot core R,G,B, mid glow R,G,B, outer glow R,G,B }
    private static final float[][] COLORS = {
            // 0 = Orange
            { 1.0f, 0.93f, 0.8f,   1.0f, 0.6f, 0.1f,   1.0f, 0.35f, 0.02f },
            // 1 = Blue
            { 0.85f, 0.92f, 1.0f,  0.2f, 0.5f, 1.0f,   0.1f, 0.3f, 1.0f },
            // 2 = Red (Homelander advanced)
            { 1.0f, 0.85f, 0.8f,   1.0f, 0.15f, 0.05f, 1.0f, 0.05f, 0.02f },
            // 3 = Green
            { 0.85f, 1.0f, 0.85f,  0.15f, 1.0f, 0.2f,  0.05f, 0.8f, 0.1f },
            // 4 = Purple (Enderman, rare)
            { 0.92f, 0.8f, 1.0f,   0.6f, 0.15f, 1.0f,  0.4f, 0.05f, 0.85f },
            // 5 = Yellow (Husk, rare)
            { 1.0f, 1.0f, 0.85f,   1.0f, 0.9f, 0.15f,  0.9f, 0.75f, 0.02f },
            // 6 = Chest Blast (Soldier Boy — wide gold beam)
            { 1.0f, 0.95f, 0.7f,   1.0f, 0.75f, 0.15f,  1.0f, 0.5f, 0.05f },
            // 7 = Rainbow (cycles through hues)
            { 1.0f, 1.0f, 1.0f,   1.0f, 0.0f, 0.0f,   1.0f, 0.0f, 0.0f },
    };

    /** Compute cycling rainbow colors from game time. */
    private static float[] getRainbowColors(float time) {
        float hue1 = (time * 0.05f) % 1.0f;
        float hue2 = (hue1 + 0.1f) % 1.0f;
        float hue3 = (hue1 + 0.2f) % 1.0f;
        float[] core = hsbToRgb(hue1, 0.3f, 1.0f);
        float[] mid = hsbToRgb(hue2, 1.0f, 1.0f);
        float[] outer = hsbToRgb(hue3, 1.0f, 0.85f);
        return new float[] { core[0], core[1], core[2], mid[0], mid[1], mid[2], outer[0], outer[1], outer[2] };
    }

    /** Simple HSB to RGB. h/s/b in [0,1], returns float[3] RGB in [0,1]. */
    private static float[] hsbToRgb(float h, float s, float b) {
        float r = b, g = b, bl = b;
        if (s != 0) {
            float hh = (h - (float) Math.floor(h)) * 6.0f;
            int i = (int) hh;
            float f = hh - i;
            float p = b * (1 - s), q = b * (1 - s * f), t = b * (1 - s * (1 - f));
            switch (i) {
                case 0 -> { r = b; g = t; bl = p; }
                case 1 -> { r = q; g = b; bl = p; }
                case 2 -> { r = p; g = b; bl = t; }
                case 3 -> { r = p; g = q; bl = b; }
                case 4 -> { r = t; g = p; bl = b; }
                case 5 -> { r = b; g = p; bl = q; }
            }
        }
        return new float[] { r, g, bl };
    }

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
            if (!(entity instanceof LivingEntity living)) continue;

            LaserClientData.LaserInfo info = entry.getValue();
            Vec3 hitPos = new Vec3(info.hitX, info.hitY, info.hitZ);

            // --- Color selection from palette ---
            int ci = Math.max(0, Math.min(info.colorIndex, COLORS.length - 1));
            float[] c;
            if (info.colorIndex == S2CLaserSyncPacket.COLOR_RAINBOW) {
                c = getRainbowColors((living.tickCount + partialTick));
            } else {
                c = COLORS[ci];
            }
            float wr = c[0], wg = c[1], wb = c[2];  // white-hot core
            float cr = c[3], cg = c[4], cb = c[5];  // mid glow
            float gr = c[6], gg = c[7], gb = c[8];  // outer glow

            // Subtle flicker
            float time = (living.tickCount + partialTick) * 0.8f;
            float flicker = 0.92f + 0.08f * Mth.sin(time * 6.0f);
            float glowFlicker = 0.85f + 0.15f * Mth.sin(time * 3.5f + 1.0f);

            poseStack.pushPose();
            poseStack.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = poseStack.last().pose();

            if (living instanceof Player player) {
                // --- Chest Blast: wide single beam from chest ---
                if (info.colorIndex == S2CLaserSyncPacket.COLOR_CHEST_BLAST) {
                    Vec3 chestPos = player.getPosition(partialTick).add(0, player.getBbHeight() * 0.6, 0);
                    Vec3 lookDir = player.getViewVector(partialTick);

                    // Push origin slightly forward so beam doesn't clip into body
                    Vec3 beamStart = chestPos.add(lookDir.scale(0.5));

                    // Wide beam — 3 layers, progressively larger
                    float blastCore = 0.15f;
                    float blastGlow = 0.4f;
                    float blastOuter = 0.7f;

                    renderBeaconBeam(consumer, matrix, beamStart, hitPos, wr, wg, wb, 0.9f * flicker, blastCore);
                    renderBeaconBeam(consumer, matrix, beamStart, hitPos, cr, cg, cb, 0.5f * flicker, blastGlow);
                    renderBeaconBeam(consumer, matrix, beamStart, hitPos, gr, gg, gb, 0.15f * glowFlicker, blastOuter);

                } else {
                // --- Player: dual-eye beams using view yaw ---
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

                renderDualBeam(consumer, matrix, leftEye, rightEye, hitPos,
                        wr, wg, wb, cr, cg, cb, gr, gg, gb, flicker, glowFlicker,
                        CORE_HALF, GLOW_HALF, OUTER_HALF);
                }
            } else {
                // --- Mob rendering ---
                if (info.colorIndex == S2CLaserSyncPacket.COLOR_CHEST_BLAST) {
                    // Mob chest blast: wide beam from chest height
                    Vec3 mobPos = new Vec3(
                            Mth.lerp(partialTick, living.xo, living.getX()),
                            Mth.lerp(partialTick, living.yo, living.getY()) + living.getBbHeight() * 0.6,
                            Mth.lerp(partialTick, living.zo, living.getZ()));
                    Vec3 beamDir = hitPos.subtract(mobPos).normalize();
                    Vec3 beamStart = mobPos.add(beamDir.scale(0.5));

                    float blastCore = 0.12f;
                    float blastGlow = 0.3f;
                    float blastOuter = 0.5f;

                    renderBeaconBeam(consumer, matrix, beamStart, hitPos, wr, wg, wb, 0.9f * flicker, blastCore);
                    renderBeaconBeam(consumer, matrix, beamStart, hitPos, cr, cg, cb, 0.5f * flicker, blastGlow);
                    renderBeaconBeam(consumer, matrix, beamStart, hitPos, gr, gg, gb, 0.15f * glowFlicker, blastOuter);
                } else {
                // --- Mob: dual-eye beams using head yaw ---
                float headYaw = Mth.lerp(partialTick, living.yHeadRotO, living.yHeadRot);
                float headYawRad = (float) Math.toRadians(headYaw);

                // Interpolated position + eye height
                Vec3 eyeCenter = new Vec3(
                        Mth.lerp(partialTick, living.xo, living.getX()),
                        Mth.lerp(partialTick, living.yo, living.getY()) + living.getEyeHeight(),
                        Mth.lerp(partialTick, living.zo, living.getZ()));

                // Scale eye spacing to mob width (tighter for small mobs, wider for big ones)
                float mobWidth = living.getBbWidth();
                float mobEyeSpacing = mobWidth * 0.15f; // ~0.09 for zombies (0.6 wide), ~0.15 for 1.0 wide mobs
                float mobForwardOffset = mobWidth * 0.4f;

                // Direction mob is looking
                double fwdX = -Math.sin(headYawRad);
                double fwdZ = Math.cos(headYawRad);
                // Right vector (perpendicular to forward on XZ plane)
                double rightX = -Math.cos(headYawRad);
                double rightZ = -Math.sin(headYawRad);

                Vec3 forward = new Vec3(fwdX * mobForwardOffset, 0, fwdZ * mobForwardOffset);

                Vec3 leftEye = eyeCenter.add(
                        rightX * -mobEyeSpacing + forward.x,
                        forward.y,
                        rightZ * -mobEyeSpacing + forward.z);
                Vec3 rightEye = eyeCenter.add(
                        rightX * mobEyeSpacing + forward.x,
                        forward.y,
                        rightZ * mobEyeSpacing + forward.z);

                // Slightly thicker beams for mobs, scaled with size
                float mobScale = Math.max(1.0f, mobWidth);
                float mobCore = CORE_HALF * 1.1f * mobScale;
                float mobGlow = GLOW_HALF * 1.1f * mobScale;
                float mobOuter = OUTER_HALF * 1.0f * mobScale;

                renderDualBeam(consumer, matrix, leftEye, rightEye, hitPos,
                        wr, wg, wb, cr, cg, cb, gr, gg, gb, flicker, glowFlicker,
                        mobCore, mobGlow, mobOuter);
                }
            }

            poseStack.popPose();
        }

        bufferSource.endBatch(RenderType.lightning());
    }

    /**
     * Renders dual beams (left eye + right eye), each with 3 layers.
     */
    private static void renderDualBeam(VertexConsumer consumer, Matrix4f matrix,
                                        Vec3 leftEye, Vec3 rightEye, Vec3 hitPos,
                                        float wr, float wg, float wb,
                                        float cr, float cg, float cb,
                                        float gr, float gg, float gb,
                                        float flicker, float glowFlicker,
                                        float coreHalf, float glowHalf, float outerHalf) {
        // Left eye beam (3 layers)
        renderBeaconBeam(consumer, matrix, leftEye, hitPos, wr, wg, wb, 1.0f * flicker, coreHalf);
        renderBeaconBeam(consumer, matrix, leftEye, hitPos, cr, cg, cb, 0.7f * flicker, glowHalf);
        renderBeaconBeam(consumer, matrix, leftEye, hitPos, gr, gg, gb, 0.2f * glowFlicker, outerHalf);

        // Right eye beam (3 layers)
        renderBeaconBeam(consumer, matrix, rightEye, hitPos, wr, wg, wb, 1.0f * flicker, coreHalf);
        renderBeaconBeam(consumer, matrix, rightEye, hitPos, cr, cg, cb, 0.7f * flicker, glowHalf);
        renderBeaconBeam(consumer, matrix, rightEye, hitPos, gr, gg, gb, 0.2f * glowFlicker, outerHalf);
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
