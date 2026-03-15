package blueduck.compound_v.client;

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
import net.minecraft.world.effect.MobEffects;

/**
 * Renders a gray stone-colored overlay on top of the player's skin
 * when the Density effect is active (dense mode toggled on).
 *
 * Detects dense mode by checking for both the DENSITY compound V effect
 * and the MOVEMENT_SLOWDOWN vanilla effect (which DensityEffect applies while dense).
 */
public class DensityRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public DensityRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // Proxy check: density effect + slowness = dense mode is on
        if (!player.hasEffect(EffectReg.DENSITY.get())) return;
        if (!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) return;

        // Render the player model again with a gray stone tint overlay
        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityTranslucent(player.getSkinTextureLocation()));

        // Dark stone color — heavy desaturation
        this.getParentModel().renderToBuffer(poseStack, consumer, packedLight,
                OverlayTexture.NO_OVERLAY, 0.3f, 0.3f, 0.32f, 0.85f);
    }
}
