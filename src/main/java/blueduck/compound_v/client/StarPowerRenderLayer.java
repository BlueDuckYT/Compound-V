package blueduck.compound_v.client;

import blueduck.compound_v.effect.StarPowerEffect;
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

/**
 * Renders a bright, cycling rainbow overlay on the player when Star Power
 * is active. Uses entityTranslucentEmissive so the player glows brightly
 * even in darkness — fully self-lit like a beacon.
 *
 * Color cycles through the rainbow spectrum using sine waves on RGB channels,
 * each offset by 120 degrees for smooth hue rotation.
 */
public class StarPowerRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public StarPowerRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!player.hasEffect(EffectReg.STAR_POWER.get())) return;
        if (!StarPowerEffect.isStarActive(player.getUUID())) return;

        // Cycle hue using sine waves offset by 2π/3 (120°) for smooth rainbow
        float time = ageInTicks * 0.15f;
        float r = (float) (Math.sin(time) * 0.5 + 0.5);
        float g = (float) (Math.sin(time + 2.094) * 0.5 + 0.5); // +2π/3
        float b = (float) (Math.sin(time + 4.189) * 0.5 + 0.5); // +4π/3

        // Brighten — clamp minimum so it's always vivid, never dark
        r = 0.4f + r * 0.6f;
        g = 0.4f + g * 0.6f;
        b = 0.4f + b * 0.6f;

        // Use emissive render type so the overlay glows at full brightness in darkness
        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityTranslucentEmissive(player.getSkinTextureLocation()));

        // Full brightness light, semi-transparent overlay
        this.getParentModel().renderToBuffer(poseStack, consumer, 0xF000F0,
                OverlayTexture.NO_OVERLAY, r, g, b, 0.7f);
    }
}
