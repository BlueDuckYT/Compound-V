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
 * Renders a translucent green slime overlay on the player while Slime mode is active.
 *
 * Slime's active state lives server-side, so (like DensityRenderLayer) we detect it via a proxy:
 * the SLIME compound V effect plus the hidden Jump Boost marker that SlimeEffect applies only
 * while toggled on.
 */
public class SlimeRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public SlimeRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // Proxy check: SLIME effect + the hidden Jump marker = slime mode is on.
        if (!player.hasEffect(EffectReg.SLIME.get())) return;
        if (!player.hasEffect(MobEffects.JUMP)) return;

        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityTranslucent(player.getSkinTextureLocation()));

        // Translucent slime green.
        this.getParentModel().renderToBuffer(poseStack, consumer, packedLight,
                OverlayTexture.NO_OVERLAY, 0.3f, 0.85f, 0.35f, 0.6f);
    }
}
