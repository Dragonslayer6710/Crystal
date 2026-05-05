package com.crystal.engine.input;

import static org.lwjgl.glfw.GLFW.*;

public enum MouseButton {

    LMB(GLFW_MOUSE_BUTTON_1),
    RMB(GLFW_MOUSE_BUTTON_2),
    MMB(GLFW_MOUSE_BUTTON_3);

    private final int code;

    MouseButton(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MouseButton fromCode(int code) {
        for (MouseButton btn : values()) {
            if (btn.code == code) {
                return btn;
            }
        }

        return null;
    }
}