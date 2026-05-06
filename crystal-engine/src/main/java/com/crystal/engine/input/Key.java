package com.crystal.engine.input;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public enum Key {
    W(GLFW_KEY_W),
    A(GLFW_KEY_A),
    S(GLFW_KEY_S),
    D(GLFW_KEY_D),

    F(GLFW_KEY_F),

    SPACE(GLFW_KEY_SPACE),
    LEFT_SHIFT(GLFW_KEY_LEFT_SHIFT),
    LEFT_CTRL(GLFW_KEY_LEFT_CONTROL),

    ESCAPE(GLFW_KEY_ESCAPE);

    private static final Map<Integer, Key> LOOKUP = new HashMap<>();

    static {
        for (Key key : values()) {
            LOOKUP.put(key.code, key);
        }
    }

    private final int code;

    Key(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Key fromCode(int code) {
        return LOOKUP.get(code);
    }
}