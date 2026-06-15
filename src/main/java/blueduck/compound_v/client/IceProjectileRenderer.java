package blueduck.compound_v.client;

import blueduck.compound_v.entity.IceProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/**
 * Renders ice projectiles as the snowball item, but scales up the big CHARGED cryoball so it
 * reads as a larger, clunkier projectile.
 */
public class IceProjectileRenderer extends ThrownItemRenderer<IceProjectileEntity> {

    public IceProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(IceProjectileEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isCharged()) {
            poseStack.pushPose();
            poseStack.scale(2.4F, 2.4F, 2.4F); // big clunky charged ball
            super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
            poseStack.popPose();
        } else {
            super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
        }
    }
}
