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

}
