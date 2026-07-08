package blueduck.compound_v.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {

    public static final String KEY_CATEGORY = "key.category.compound_v.compound_v";
    public static final String KEY_ACTIVATE = "key.category.compound_v.activate_power";

    public static final KeyMapping POWER_KEY = new KeyMapping(KEY_ACTIVATE, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY);

    /**
     * Robust "is the power key held" check. {@link KeyMapping#isDown()} can desync (get stuck
     * true/false) across game-state transitions — notably spectator→survival and dimension
     * changes — because the press/release events don't always fire across the transition. Reading
     * the raw hardware key state via GLFW avoids that, so scroll/hold behavior keeps working.
     * Falls back to the mapping's own state if the key isn't a simple KEYSYM (e.g. rebound to a
     * mouse button).
     */
    public static boolean isPowerKeyHeld() {
        try {
            InputConstants.Key key = POWER_KEY.getKey();
            if (key.getType() == InputConstants.Type.KEYSYM && key.getValue() != InputConstants.UNKNOWN.getValue()) {
                long window = net.minecraft.client.Minecraft.getInstance().getWindow().getWindow();
                return InputConstants.isKeyDown(window, key.getValue());
            }
            if (key.getType() == InputConstants.Type.MOUSE) {
                long window = net.minecraft.client.Minecraft.getInstance().getWindow().getWindow();
                return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            }
        } catch (Exception ignored) {
        }
        return POWER_KEY.isDown();
    }
}
