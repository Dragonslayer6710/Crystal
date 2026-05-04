package com.crystal.engine.input;

import com.crystal.engine.window.Window;

import static org.lwjgl.glfw.GLFW.*;

public class Input {

    private final Window window;

    public Input(Window window) {
        this.window = window;
    }

    public boolean isKeyDown(Key key) {
        return glfwGetKey(window.getHandle(), key.getCode()) == GLFW_PRESS;
    }

    public boolean isKeyUp(Key key) {
        return glfwGetKey(window.getHandle(), key.getCode()) == GLFW_RELEASE;
    }
}
