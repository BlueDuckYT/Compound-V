package blueduck.compound_v.client;

import blueduck.compound_v.effect.ForcefieldEffect;
import blueduck.compound_v.registry.EffectReg;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Renders a glowing translucent cube around players with an active forcefield.
 * Uses energySwirl render type for the charged-creeper-style animated overlay.
 */
@OnlyIn(Dist.CLIENT)
public class ForcefieldRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation ENERGY_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");

    public ForcefieldRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!player.hasEffect(EffectReg.FORCEFIELD.get())) return;

        // Amplifier 1 = active, 0 = inactive
        var inst = player.getEffect(EffectReg.FORCEFIELD.get());
        if (inst == null || inst.getAmplifier() < 1) return;

        int color = Math.abs(player.getUUID().hashCode()) % 2;

        // Color tinting
        float r, g, b;
        if (color == 0) {
            // Pink
            r = 1.0f; g = 0.4f; b = 0.7f;
        } else {
            // Gold
            r = 1.0f; g = 0.85f; b = 0.3f;
        }

        float alpha = 0.25f + 0.05f * (float) Math.sin(ageInTicks * 0.1);
        float radius = 2.0f; // matches FIELD_RADIUS

        // Animated UV offset for the energy swirl
        float scroll = (ageInTicks * 0.01f) % 1.0f;

        poseStack.pushPose();

        // Counter the player's body yaw so the cube stays world-aligned (does not rotate).
        // LivingEntityRenderer applies mulPose(YP.rotationDegrees(180 - bodyYaw)) before layers,
        // so we undo it with the inverse rotation: bodyYaw - 180.
        float bodyYaw = net.minecraft.util.Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(bodyYaw - 180.0F));

        // Offset to center of player (render is relative to entity feet)
        poseStack.translate(0, player.getBbHeight() * 0.5, 0);

        VertexConsumer consumer = buffer.getBuffer(
                RenderType.energySwirl(ENERGY_TEXTURE, scroll, scroll));
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Render 6 faces of the cube
        renderFace(consumer, matrix, normal, r, g, b, alpha,
                -radius, -radius, -radius, radius, radius, -radius, 0, 0, -1); // -Z
        renderFace(consumer, matrix, normal, r, g, b, alpha,
                -radius, -radius, radius, radius, radius, radius, 0, 0, 1);   // +Z
        renderFace(consumer, matrix, normal, r, g, b, alpha * 0.8f,
                -radius, -radius, -radius, -radius, radius, radius, -1, 0, 0); // -X
        renderFace(consumer, matrix, normal, r, g, b, alpha * 0.8f,
                radius, -radius, -radius, radius, radius, radius, 1, 0, 0);    // +X
        renderFace(consumer, matrix, normal, r, g, b, alpha * 0.6f,
                -radius, radius, -radius, radius, radius, radius, 0, 1, 0);    // +Y (top)
        renderFace(consumer, matrix, normal, r, g, b, alpha * 0.6f,
                -radius, -radius, -radius, radius, -radius, radius, 0, -1, 0); // -Y (bottom)

        poseStack.popPose();
    }

    /**
     * Render a single face of the force field cube.
     */
    private void renderFace(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                            float r, float g, float b, float a,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float nx, float ny, float nz) {
        // Build quad from the min/max corners
        float ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz;

        if (nx != 0) {
            // X-facing face
            ax = x1; ay = y1; az = z1;
            bx = x1; by = y2; bz = z1;
            cx = x1; cy = y2; cz = z2;
            dx = x1; dy = y1; dz = z2;
        } else if (ny != 0) {
            // Y-facing face
            ax = x1; ay = y1; az = z1;
            bx = x2; by = y1; bz = z1;
            cx = x2; cy = y1; cz = z2;
            dx = x1; dy = y1; dz = z2;
        } else {
            // Z-facing face
            ax = x1; ay = y1; az = z1;
            bx = x2; by = y1; bz = z1;
            cx = x2; cy = y2; cz = z1;
            dx = x1; dy = y2; dz = z1;
        }

        int light = 0xF000F0; // full bright
        consumer.vertex(matrix, ax, ay, az).color(r, g, b, a)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light).normal(normal, nx, ny, nz).endVertex();
        consumer.vertex(matrix, bx, by, bz).color(r, g, b, a)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light).normal(normal, nx, ny, nz).endVertex();
        consumer.vertex(matrix, cx, cy, cz).color(r, g, b, a)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light).normal(normal, nx, ny, nz).endVertex();
        consumer.vertex(matrix, dx, dy, dz).color(r, g, b, a)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light).normal(normal, nx, ny, nz).endVertex();
    }
}
