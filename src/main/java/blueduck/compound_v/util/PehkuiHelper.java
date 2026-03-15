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
}
