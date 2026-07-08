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

        // Forcefield active-state and health are broadcast to all trackers AND self via
        // S2CForcefieldSyncPacket -> ClientForcefieldData, so the render reads the same synced
        // source for everyone. (No amplifier or server-side map is consulted client-side.)
        if (!player.hasEffect(EffectReg.FORCEFIELD.get())) return;
        boolean active = blueduck.compound_v.util.ClientForcefieldData.isActive(player.getId());
        if (!active) return;

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

        // Shield health drives transparency: full = bright, low = faint. Shimmer and UV scroll run
        // at a constant rate regardless of health.
        float frac = blueduck.compound_v.util.ClientForcefieldData.getHealth(player.getId());
        float baseAlpha = 0.08f + 0.22f * frac;
        float shimmer = 0.04f * (float) Math.sin(ageInTicks * 0.08);
        float alpha = baseAlpha + shimmer;
        float radius = 1.15f;

        // Constant UV scroll speed (independent of health).
        float scrollSpeed = 0.014f;
        float scroll = (ageInTicks * scrollSpeed) % 1.0f;

        poseStack.pushPose();

        // Coordinate-space note: RenderLayer runs inside LivingEntityRenderer's model space, which
        // is FLIPPED - the model applies scale(-1,-1,1), so here +Y points DOWN and the origin is
        // at the player's HEAD, not the feet. We correct it once, up front: flip Y back to +Y-up
        // and move the origin to the feet, after which bottomY/topY are intuitive world coordinates
        // measured up from the feet.
        poseStack.scale(1.0F, -1.0F, 1.0F);                 // undo the model's Y flip (+Y now up)
        poseStack.translate(0.0F, -player.getBbHeight(), 0.0F); // origin from head -> feet

        // Counter the player's body yaw so the cube stays world-aligned (does not rotate with
        // the body). The renderer applied YP.rotationDegrees(180 - bodyYaw); undo it.
        float bodyYaw = net.minecraft.util.Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(bodyYaw - 180.0F));

        // Now in a clean feet-origin, +Y-up frame: enclose the body with a little headroom.
        float bottomY = -0.05f;                                  // just below the feet
        float topY = Math.max(player.getBbHeight() + 0.7f, 2.5f); // above the head

        VertexConsumer consumer = buffer.getBuffer(
                RenderType.energySwirl(ENERGY_TEXTURE, scroll, scroll));
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Render 6 faces of the cube. X/Z use the horizontal radius; Y spans bottomY..topY.
        renderFace(consumer, matrix, normal, r, g, b, alpha,
                -radius, bottomY, -radius, radius, topY, -radius, 0, 0, -1); // -Z
        renderFace(consumer, matrix, normal, r, g, b, alpha,
                -radius, bottomY, radius, radius, topY, radius, 0, 0, 1);   // +Z
        renderFace(consumer, matrix, normal, r, g, b, alpha * 0.8f,
                -radius, bottomY, -radius, -radius, topY, radius, -1, 0, 0); // -X
        renderFace(consumer, matrix, normal, r, g, b, alpha * 0.8f,
                radius, bottomY, -radius, radius, topY, radius, 1, 0, 0);    // +X
        renderFace(consumer, matrix, normal, r, g, b, alpha * 0.6f,
                -radius, topY, -radius, radius, topY, radius, 0, 1, 0);    // +Y (top)
        renderFace(consumer, matrix, normal, r, g, b, alpha * 0.6f,
                -radius, bottomY, -radius, radius, bottomY, radius, 0, -1, 0); // -Y (bottom)

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
