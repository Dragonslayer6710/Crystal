package com.crystal.engine.input;

import static org.lwjgl.glfw.GLFW.*;

public enum Key {

    W(GLFW_KEY_W),
    A(GLFW_KEY_A),
    S(GLFW_KEY_S),
    D(GLFW_KEY_D),

    SPACE(GLFW_KEY_SPACE),
    LEFT_SHIFT(GLFW_KEY_LEFT_SHIFT),
    LEFT_CTRL(GLFW_KEY_LEFT_CONTROL),

    ESCAPE(GLFW_KEY_ESCAPE),

    MOUSE_LEFT(GLFW_MOUSE_BUTTON_1);

    private final int code;

    Key(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}