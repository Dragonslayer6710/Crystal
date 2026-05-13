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
    P(GLFW_KEY_P),

    NUMPAD_0(GLFW_KEY_KP_0),
    NUMPAD_1(GLFW_KEY_KP_1),
    NUMPAD_2(GLFW_KEY_KP_2),
    NUMPAD_3(GLFW_KEY_KP_3),
    NUMPAD_4(GLFW_KEY_KP_4),
    NUMPAD_5(GLFW_KEY_KP_5),
    NUMPAD_6(GLFW_KEY_KP_6),
    NUMPAD_7(GLFW_KEY_KP_7),
    NUMPAD_8(GLFW_KEY_KP_8),
    NUMPAD_9(GLFW_KEY_KP_9),
    NUMPAD_ENTER(GLFW_KEY_KP_ENTER),

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