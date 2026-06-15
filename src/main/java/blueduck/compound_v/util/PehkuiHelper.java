package blueduck.compound_v.util;

import net.minecraft.world.entity.LivingEntity;

/**
 * Helper class that directly references Pehkui classes.
 * This class should NEVER be loaded unless Pehkui is confirmed present.
 * All calls should go through ShrinkEffect which checks ModList first.
 */
public class PehkuiHelper {

    /**
     * Shrink the entity to the given scale using Pehkui's BASE scale type.
     */
    public static void setScale(LivingEntity entity, float scale) {
        try {
            virtuoel.pehkui.api.ScaleData scaleData = virtuoel.pehkui.api.ScaleTypes.BASE.getScaleData(entity);
            scaleData.setTargetScale(scale);
            scaleData.setScaleTickDelay(10); // Smooth transition over 10 ticks
        } catch (Exception e) {
            // Safety catch - should not happen if Pehkui is loaded
            com.mojang.logging.LogUtils.getLogger().warn("Compound V: Failed to set Pehkui scale", e);
        }
    }

    /**
     * Reset entity to normal scale.
     */
    public static void resetScale(LivingEntity entity) {
        try {
            virtuoel.pehkui.api.ScaleData scaleData = virtuoel.pehkui.api.ScaleTypes.BASE.getScaleData(entity);
            scaleData.setTargetScale(1.0f);
            scaleData.setScaleTickDelay(10);
        } catch (Exception e) {
            com.mojang.logging.LogUtils.getLogger().warn("Compound V: Failed to reset Pehkui scale", e);
        }
    }

    /**
     * Get the current target scale of the entity.
     */
    public static float getTargetScale(LivingEntity entity) {
        try {
            virtuoel.pehkui.api.ScaleData scaleData = virtuoel.pehkui.api.ScaleTypes.BASE.getScaleData(entity);
            return scaleData.getTargetScale();
        } catch (Exception e) {
            return 1.0f;
        }
    }

    /**
     * Get the entity's CURRENT (interpolated) scale — where the body actually is right
     * now, not the target it is easing toward. Used to drive smooth size-derived stats.
     */
    public static float getCurrentScale(LivingEntity entity) {
        try {
            virtuoel.pehkui.api.ScaleData scaleData = virtuoel.pehkui.api.ScaleTypes.BASE.getScaleData(entity);
            return scaleData.getScale();
        } catch (Exception e) {
            return 1.0f;
        }
    }

    /**
     * Set the target scale with a custom interpolation delay (in ticks). A small,
     * consistent delay lets repeated calls ramp smoothly instead of restarting a long
     * ease each time, which is what produced the stair-step feel on fast scrolling.
     */
    public static void setScaleSmooth(LivingEntity entity, float scale, int tickDelay) {
        try {
            virtuoel.pehkui.api.ScaleData scaleData = virtuoel.pehkui.api.ScaleTypes.BASE.getScaleData(entity);
            scaleData.setTargetScale(scale);
            scaleData.setScaleTickDelay(tickDelay);
        } catch (Exception e) {
            com.mojang.logging.LogUtils.getLogger().warn("Compound V: Failed to set Pehkui scale", e);
        }
    }
}
