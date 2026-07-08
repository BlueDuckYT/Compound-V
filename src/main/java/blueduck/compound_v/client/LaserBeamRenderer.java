package blueduck.compound_v.client;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.util.S2CLaserSyncPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
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

    // Beam widths - inner core is tight and bright, outer glow is wide
    private static final float CORE_HALF = 0.04f;
    private static final float GLOW_HALF = 0.12f;
    private static final float OUTER_HALF = 0.2f;

    /**
     * Opaque, depth-writing, texture-free render type used for the black-hole void
     * core. Uses POSITION_COLOR (matching addQuad's vertex writes - no normal/UV),
     * with no blending so the black actually occludes the world behind it. Defined
     * via a tiny RenderType subclass because RenderType.create is protected.
     */
    private static final RenderType VOID_CORE = VoidCoreType.create();

    private static final class VoidCoreType extends RenderType {
        // Never instantiated; exists only to expose the protected static
        // RenderType.create / CompositeState API to this class.
        private VoidCoreType() {
            super(null, null, null, 0, false, false, null, null);
        }

        static RenderType create() {
            return RenderType.create(
                    "compound_v_void_core",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    false,
                    RenderType.CompositeState.builder()
                            .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                            .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                            .setCullState(RenderStateShard.CULL)
                            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                            .createCompositeState(false));
        }
    }

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
            // 6 = Chest Blast (Soldier Boy - wide gold beam)
            { 1.0f, 0.95f, 0.7f,   1.0f, 0.75f, 0.15f,  1.0f, 0.5f, 0.05f },
            // 7 = Rainbow (cycles through hues)
            { 1.0f, 1.0f, 1.0f,   1.0f, 0.0f, 0.0f,   1.0f, 0.0f, 0.0f },
            // 8 = Black (black-hole: pitch-black core handled specially, violet accretion glow)
            { 0.0f, 0.0f, 0.0f,   0.45f, 0.05f, 0.65f,   0.6f, 0.1f, 0.95f },
            // 9 = White (bright white core, silver edge)
            { 1.0f, 1.0f, 1.0f,   0.95f, 0.95f, 1.0f,  0.85f, 0.85f, 0.9f },
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
                // Decide SHAPE by whether the player actually has the Chest Blast effect, NOT by
                // the color index - otherwise a recolored chest blast (green/black/etc.) would
                // fall through to the dual-eye laser shape. Color and shape are independent now.
                boolean isChestBlast = player.hasEffect(blueduck.compound_v.registry.EffectReg.CHEST_BLAST.get());
                if (isChestBlast) {
                    Vec3 chestPos = player.getPosition(partialTick).add(0, player.getBbHeight() * 0.6, 0);
                    Vec3 lookDir = player.getViewVector(partialTick);

                    // Push origin slightly forward so beam doesn't clip into body
                    Vec3 beamStart = chestPos.add(lookDir.scale(0.5));

                    // Wide beam - 3 layers, progressively larger
                    float blastCore = 0.15f;
                    float blastGlow = 0.4f;
                    float blastOuter = 0.7f;

                    if (info.colorIndex == S2CLaserSyncPacket.COLOR_BLACK) {
                        renderBeamBlackHole(bufferSource, matrix, beamStart, hitPos,
                                cr, cg, cb, gr, gg, gb, flicker, glowFlicker,
                                blastCore, blastGlow, blastOuter);
                        consumer = bufferSource.getBuffer(RenderType.lightning());
                    } else {
                        renderBeaconBeam(consumer, matrix, beamStart, hitPos, wr, wg, wb, 0.9f * flicker, blastCore);
                        renderBeaconBeam(consumer, matrix, beamStart, hitPos, cr, cg, cb, 0.5f * flicker, blastGlow);
                        renderBeaconBeam(consumer, matrix, beamStart, hitPos, gr, gg, gb, 0.15f * glowFlicker, blastOuter);
                    }

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

                // Intimidation mode (flagged by tiny intensity): render two SHORT stubs instead
                // of a full beam, in ALL views. The stubs run along the player's look direction
                // (stable - avoids the jitter from each eye->hitPos vector swinging while moving).
                //   * Third person: the two stubs are PARALLEL (same lookDir step from each eye).
                //   * First person: the two stubs are SLIGHTLY NON-PARALLEL - each angled a touch
                //     outward - so they read as two distinct beams from the camera rather than one.
                Vec3 leftTarget = hitPos;
                Vec3 rightTarget = hitPos;
                Vec3 leftStart = leftEye;
                Vec3 rightStart = rightEye;
                boolean intimidation = info.intensity <= 0.05f;
                if (intimidation) {
                    boolean firstPerson = player == mc.player
                            && mc.options.getCameraType().isFirstPerson();
                    // Don't render the intimidation glow in first person by default - it clutters
                    // your own view. Third person and other players are unaffected. Config-toggleable.
                    if (firstPerson && !blueduck.compound_v.Config.laserIntimidationFirstPerson) {
                        poseStack.popPose();
                        continue;
                    }
                    // First person reads better a touch shorter; third person a bit longer.
                    final double STUB_LEN = firstPerson ? 0.5 : 0.6;

                    // Start the stubs at the real eye positions so they're properly separated
                    // (not squeezed into one). leftStart/rightStart already = leftEye/rightEye.
                    Vec3 step = lookDir.scale(STUB_LEN);
                    if (firstPerson) {
                        // Gentle INWARD splay so they angle slightly toward each other.
                        Vec3 rightDir = new Vec3(-Math.cos(yawRad), 0, -Math.sin(yawRad)).normalize();
                        Vec3 splay = rightDir.scale(STUB_LEN * 0.07);
                        leftTarget = leftStart.add(step).add(splay);
                        rightTarget = rightStart.add(step).subtract(splay);
                    } else {
                        // Third person: parallel stubs, nudged up ~half a pixel (1px = 1/16 block).
                        Vec3 up = new Vec3(0, 0.03, 0);
                        leftStart = leftStart.add(up);
                        rightStart = rightStart.add(up);
                        leftTarget = leftStart.add(step);
                        rightTarget = rightStart.add(step);
                    }
                }

                // FIRST-PERSON OPACITY: scale the beam alpha for the LOCAL player in first person
                // only, leaving third person (and other players) completely untouched. This lets
                // you make your own lasers less obtrusive over your view without changing how they
                // look to anyone else or in third person.
                boolean fpSelf = player == mc.player
                        && mc.options.getCameraType().isFirstPerson();
                if (fpSelf) {
                    float fpOpacity = (float) blueduck.compound_v.Config.laserFirstPersonOpacity;
                    flicker *= fpOpacity;
                    glowFlicker *= fpOpacity;
                }

                // Scale beam girth by laser intensity: a low-power "glow" is a thin wisp, full
                // power is the full beam. Keep a floor so it's always visible.
                float iScale = 0.35f + 0.65f * Math.max(0f, Math.min(1f, info.intensity));
                float coreH = CORE_HALF * iScale;
                float glowH = GLOW_HALF * iScale;
                float outerH = OUTER_HALF * iScale;

                if (info.colorIndex == S2CLaserSyncPacket.COLOR_BLACK) {
                    renderDualBlackHole(bufferSource, matrix,
                            leftStart, rightStart, leftTarget, rightTarget,
                            cr, cg, cb, gr, gg, gb, flicker, glowFlicker,
                            coreH, glowH, outerH);
                    consumer = bufferSource.getBuffer(RenderType.lightning());
                } else {
                renderDualBeam(consumer, matrix, leftStart, rightStart, leftTarget, rightTarget,
                        wr, wg, wb, cr, cg, cb, gr, gg, gb, flicker, glowFlicker,
                        coreH, glowH, outerH);
                }
                }
            } else {
                // --- Mob rendering ---
                // Shape by effect presence (not color index) so recolored mob chest blasts still
                // render as the wide torso beam instead of falling through to dual-eye lasers.
                if (living.hasEffect(blueduck.compound_v.registry.EffectReg.CHEST_BLAST.get())) {
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

                    if (info.colorIndex == S2CLaserSyncPacket.COLOR_BLACK) {
                        renderBeamBlackHole(bufferSource, matrix, beamStart, hitPos,
                                cr, cg, cb, gr, gg, gb, flicker, glowFlicker,
                                blastCore, blastGlow, blastOuter);
                        consumer = bufferSource.getBuffer(RenderType.lightning());
                    } else {
                        renderBeaconBeam(consumer, matrix, beamStart, hitPos, wr, wg, wb, 0.9f * flicker, blastCore);
                        renderBeaconBeam(consumer, matrix, beamStart, hitPos, cr, cg, cb, 0.5f * flicker, blastGlow);
                        renderBeaconBeam(consumer, matrix, beamStart, hitPos, gr, gg, gb, 0.15f * glowFlicker, blastOuter);
                    }
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

                if (info.colorIndex == S2CLaserSyncPacket.COLOR_BLACK) {
                    renderDualBlackHole(bufferSource, matrix,
                            leftEye, rightEye, hitPos, hitPos,
                            cr, cg, cb, gr, gg, gb, flicker, glowFlicker,
                            mobCore, mobGlow, mobOuter);
                    consumer = bufferSource.getBuffer(RenderType.lightning());
                } else {
                renderDualBeam(consumer, matrix, leftEye, rightEye, hitPos, hitPos,
                        wr, wg, wb, cr, cg, cb, gr, gg, gb, flicker, glowFlicker,
                        mobCore, mobGlow, mobOuter);
                }
                }
            }

            poseStack.popPose();
        }

        bufferSource.endBatch(RenderType.lightning());
    }

    /**
     * Renders dual "black-hole" beams. The core is drawn as an OPAQUE pitch-black
     * tube in a depth-writing, non-additive render type (RenderType.leash) so it
     * genuinely occludes the world behind it and reads as a true void - additive
     * blending alone can never produce black. A bright violet accretion halo is
     * then layered additively (lightning) around the void.
     *
     * @param cr,cg,cb inner accretion (violet) color
     * @param gr,gg,gb outer accretion (violet) color
     */
    private static void renderDualBlackHole(MultiBufferSource.BufferSource bufferSource, Matrix4f matrix,
                                            Vec3 leftEye, Vec3 rightEye, Vec3 leftTarget, Vec3 rightTarget,
                                            float cr, float cg, float cb,
                                            float gr, float gg, float gb,
                                            float flicker, float glowFlicker,
                                            float coreHalf, float glowHalf, float outerHalf) {
        // --- 1. Opaque pitch-black void core (occludes scenery) ---
        VertexConsumer opaque = bufferSource.getBuffer(VOID_CORE);
        // Slightly fatter than a normal core so the void is clearly visible.
        float voidHalf = coreHalf * 1.6f;
        renderBeaconBeam(opaque, matrix, leftEye, leftTarget, 0.0f, 0.0f, 0.0f, 1.0f, voidHalf);
        renderBeaconBeam(opaque, matrix, rightEye, rightTarget, 0.0f, 0.0f, 0.0f, 1.0f, voidHalf);
        // Flush the opaque pass now so depth is written before the additive halo.
        bufferSource.endBatch(VOID_CORE);

        // --- 2. Violet accretion halo (additive, wraps the void) ---
        VertexConsumer glow = bufferSource.getBuffer(RenderType.lightning());
        // Inner halo: bright violet hugging the void edge
        renderBeaconBeam(glow, matrix, leftEye, leftTarget, cr, cg, cb, 0.85f * flicker, glowHalf);
        renderBeaconBeam(glow, matrix, rightEye, rightTarget, cr, cg, cb, 0.85f * flicker, glowHalf);
        // Outer halo: softer, wider violet bloom
        renderBeaconBeam(glow, matrix, leftEye, leftTarget, gr, gg, gb, 0.3f * glowFlicker, outerHalf);
        renderBeaconBeam(glow, matrix, rightEye, rightTarget, gr, gg, gb, 0.3f * glowFlicker, outerHalf);
        bufferSource.endBatch(RenderType.lightning());
    }

    /**
     * Single wide beam black-hole variant for the Chest Blast (one beam from the torso, vs the
     * dual-eye laser version). Pitch-black occluding void core wrapped in a violet accretion halo.
     */
    private static void renderBeamBlackHole(MultiBufferSource.BufferSource bufferSource, Matrix4f matrix,
                                            Vec3 start, Vec3 target,
                                            float cr, float cg, float cb,
                                            float gr, float gg, float gb,
                                            float flicker, float glowFlicker,
                                            float coreHalf, float glowHalf, float outerHalf) {
        // 1. Opaque pitch-black void core (occludes scenery), a bit fatter so it reads as a void.
        VertexConsumer opaque = bufferSource.getBuffer(VOID_CORE);
        renderBeaconBeam(opaque, matrix, start, target, 0.0f, 0.0f, 0.0f, 1.0f, coreHalf * 1.6f);
        bufferSource.endBatch(VOID_CORE);
        // 2. Violet accretion halo (additive) wrapping the void.
        VertexConsumer glow = bufferSource.getBuffer(RenderType.lightning());
        renderBeaconBeam(glow, matrix, start, target, cr, cg, cb, 0.85f * flicker, glowHalf);
        renderBeaconBeam(glow, matrix, start, target, gr, gg, gb, 0.3f * glowFlicker, outerHalf);
        bufferSource.endBatch(RenderType.lightning());
    }
    private static void renderDualBeam(VertexConsumer consumer, Matrix4f matrix,
                                        Vec3 leftEye, Vec3 rightEye, Vec3 leftTarget, Vec3 rightTarget,
                                        float wr, float wg, float wb,
                                        float cr, float cg, float cb,
                                        float gr, float gg, float gb,
                                        float flicker, float glowFlicker,
                                        float coreHalf, float glowHalf, float outerHalf) {
        // Each eye aims at its own target (normally both = the hit point, so they converge;
        // in intimidation mode each is a short clip along that same aim direction).
        // Left eye beam (3 layers)
        renderBeaconBeam(consumer, matrix, leftEye, leftTarget, wr, wg, wb, 1.0f * flicker, coreHalf);
        renderBeaconBeam(consumer, matrix, leftEye, leftTarget, cr, cg, cb, 0.7f * flicker, glowHalf);
        renderBeaconBeam(consumer, matrix, leftEye, leftTarget, gr, gg, gb, 0.2f * glowFlicker, outerHalf);

        // Right eye beam (3 layers)
        renderBeaconBeam(consumer, matrix, rightEye, rightTarget, wr, wg, wb, 1.0f * flicker, coreHalf);
        renderBeaconBeam(consumer, matrix, rightEye, rightTarget, cr, cg, cb, 0.7f * flicker, glowHalf);
        renderBeaconBeam(consumer, matrix, rightEye, rightTarget, gr, gg, gb, 0.2f * glowFlicker, outerHalf);
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

        // End caps: close the tube at both ends so it doesn't read as a hollow open-ended pipe
        // (very visible on a short intimidation stub viewed head-on).
        addQuad(consumer, matrix, s_pp, s_pn, s_nn, s_np, r, g, b, a); // start face
        addQuad(consumer, matrix, e_pp, e_np, e_nn, e_pn, r, g, b, a); // end face
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
