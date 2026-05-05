package com.crystal.engine.input;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public enum MouseButton {

    LMB(GLFW_MOUSE_BUTTON_1),
    RMB(GLFW_MOUSE_BUTTON_2),
    MMB(GLFW_MOUSE_BUTTON_3);

    private static final Map<Integer, MouseButton> LOOKUP = new HashMap<>();

    static {
        for (MouseButton btn : values()) {
            LOOKUP.put(btn.code, btn);
        }
    }

    private final int code;

    MouseButton(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MouseButton fromCode(int code) {
        return LOOKUP.get(code);
    }
}