package blueduck.compound_v.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side renderer for Stormfront ground lightning beams.
 * Uses RenderType.lightning() for the same glowing effect as laser eyes.
 *
 * Beams connect from the entity downward to ground contact points,
 * with jagged offsets for an organic lightning look.
 */
@Mod.EventBusSubscriber(modid = "compound_v", value = Dist.CLIENT)
public class StormfrontBeamRenderer {

    // Beam data per entity
    private static final Map<Integer, BeamData> activeBeams = new ConcurrentHashMap<>();
    private static final int BEAM_EXPIRE_TICKS = 3; // clear if no update for this many ticks

    // Purple/white color palette for lightning
    // Core (white-purple), Mid (bright purple), Outer (deep purple glow)
    private static final float CR = 0.95f, CG = 0.85f, CB = 1.0f;   // core: near-white
    private static final float MR = 0.7f, MG = 0.5f, MB = 1.0f;     // mid: bright purple
    private static final float OR = 0.4f, OG = 0.2f, OB = 0.8f;     // outer: deep purple

    private static class BeamData {
        double[] bx, by, bz;
        int count;
        long receivedTick;
        // Jitter offsets — generated once per beam set for stable rendering
        double[][] jitterX, jitterY, jitterZ;
    }

    /**
     * Called from packet handler on the client thread.
     */
    public static void receiveBeamData(int entityId, double[] bx, double[] by, double[] bz, int count) {
        BeamData data = new BeamData();
        data.bx = bx.clone();
        data.by = by.clone();
        data.bz = bz.clone();
        data.count = count;
        data.receivedTick = System.currentTimeMillis(); // approximate, just for expiry

        // Generate jitter segments for each beam (8 segments per beam)
        int segments = 8;
        data.jitterX = new double[count][segments + 1];
        data.jitterY = new double[count][segments + 1];
        data.jitterZ = new double[count][segments + 1];
        java.util.Random rand = new java.util.Random(entityId * 31L + System.nanoTime());
        for (int b = 0; b < count; b++) {
            data.jitterX[b][0] = 0; data.jitterY[b][0] = 0; data.jitterZ[b][0] = 0; // start: no offset
            data.jitterX[b][segments] = 0; data.jitterY[b][segments] = 0; data.jitterZ[b][segments] = 0; // end: no offset
            for (int s = 1; s < segments; s++) {
                double t = (double) s / segments;
                double spread = 0.4 * Math.sin(t * Math.PI); // more jitter in the middle
                data.jitterX[b][s] = (rand.nextDouble() - 0.5) * spread;
                data.jitterY[b][s] = (rand.nextDouble() - 0.5) * spread * 0.5;
                data.jitterZ[b][s] = (rand.nextDouble() - 0.5) * spread;
            }
        }

        activeBeams.put(entityId, data);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (activeBeams.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        float partialTick = event.getPartialTick();
        long now = System.currentTimeMillis();

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = poseStack.last().pose();

        // Flicker effect
        float flicker = 0.8f + 0.2f * (float) Math.sin(now * 0.02);
        float glowFlicker = 0.6f + 0.4f * (float) Math.sin(now * 0.015 + 1.0);

        activeBeams.entrySet().removeIf(entry -> {
            BeamData data = entry.getValue();
            // Expire after a few frames of no updates
            if (now - data.receivedTick > BEAM_EXPIRE_TICKS * 55) return true; // ~3 ticks at 50ms each

            Entity entity = mc.level.getEntity(entry.getKey());
            if (entity == null) return true;

            // Interpolated entity position
            double ex = Mth.lerp(partialTick, entity.xo, entity.getX());
            double eyFeet = Mth.lerp(partialTick, entity.yo, entity.getY());
            double eyChest = eyFeet + entity.getBbHeight() * 0.6;
            double ez = Mth.lerp(partialTick, entity.zo, entity.getZ());

            for (int b = 0; b < data.count; b++) {
                // Ground beams (endpoint below entity) originate from feet;
                // Attack beams (endpoint at or above entity) originate from chest
                double startY = data.by[b] < eyFeet ? eyFeet : eyChest;
                Vec3 start = new Vec3(ex, startY, ez);
                Vec3 end = new Vec3(data.bx[b], data.by[b], data.bz[b]);

                // Render segmented lightning bolt with jitter
                int segments = data.jitterX[b].length - 1;
                for (int s = 0; s < segments; s++) {
                    double t0 = (double) s / segments;
                    double t1 = (double) (s + 1) / segments;

                    Vec3 dir = end.subtract(start);
                    Vec3 p0 = start.add(dir.scale(t0)).add(
                            data.jitterX[b][s], data.jitterY[b][s], data.jitterZ[b][s]);
                    Vec3 p1 = start.add(dir.scale(t1)).add(
                            data.jitterX[b][s + 1], data.jitterY[b][s + 1], data.jitterZ[b][s + 1]);

                    // 3 layers: core (thin bright), mid (medium), outer (wide faint glow)
                    renderSegment(consumer, matrix, p0, p1, CR, CG, CB, 0.9f * flicker, 0.03f);
                    renderSegment(consumer, matrix, p0, p1, MR, MG, MB, 0.5f * flicker, 0.08f);
                    renderSegment(consumer, matrix, p0, p1, OR, OG, OB, 0.15f * glowFlicker, 0.15f);
                }
            }

            return false;
        });

        poseStack.popPose();
        bufferSource.endBatch(RenderType.lightning());
    }

    /**
     * Render a single beam segment between two points with billboard quads.
     * Same technique as LaserBeamRenderer.renderBeaconBeam but for short segments.
     */
    private static void renderSegment(VertexConsumer consumer, Matrix4f matrix,
                                       Vec3 from, Vec3 to, float r, float g, float b, float a, float halfWidth) {
        Vec3 dir = to.subtract(from);
        double length = dir.length();
        if (length < 0.01) return;

        // Camera-facing billboard perpendicular to beam direction
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = dir.normalize().cross(up);
        if (right.length() < 0.001) {
            right = dir.normalize().cross(new Vec3(1, 0, 0));
        }
        right = right.normalize().scale(halfWidth);

        float x0 = (float) (from.x - right.x);
        float y0 = (float) (from.y - right.y);
        float z0 = (float) (from.z - right.z);
        float x1 = (float) (from.x + right.x);
        float y1 = (float) (from.y + right.y);
        float z1 = (float) (from.z + right.z);
        float x2 = (float) (to.x + right.x);
        float y2 = (float) (to.y + right.y);
        float z2 = (float) (to.z + right.z);
        float x3 = (float) (to.x - right.x);
        float y3 = (float) (to.y - right.y);
        float z3 = (float) (to.z - right.z);

        consumer.vertex(matrix, x0, y0, z0).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, x3, y3, z3).color(r, g, b, a).endVertex();
    }
}
