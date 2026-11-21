package com.wkrs14.oretracker.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class TrackerKeys {
    public static final String CATEGORY = "key.categories.oretracker";
    public static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.oretracker.open_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            CATEGORY
    );
    public static final KeyMapping TOGGLE = new KeyMapping(
            "key.oretracker.toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    );

    private TrackerKeys() {
    }
}
