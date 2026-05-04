package com.crystal.engine.input;

import com.crystal.engine.window.Window;

import static org.lwjgl.glfw.GLFW.*;

public class Input {

    private final Window window;

    // Mouse Input
    private double lastMouseX;
    private double lastMouseY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private boolean firstMouse = true;

    public Input(Window window) {
        this.window = window;
    }

    public void update() {
        double[] x = new double[1];
        double[] y = new double[1];

        glfwGetCursorPos(window.getHandle(), x, y);

        if (firstMouse) {
            lastMouseX = x[0];
            lastMouseY = y[0];
            firstMouse = false;
        }

        mouseDeltaX = x[0] - lastMouseX;
        mouseDeltaY = y[0] - lastMouseY;

        lastMouseX = x[0];
        lastMouseY = y[0];
    }

    public double getMouseDeltaX() {
        return mouseDeltaX;
    }

    public double getMouseDeltaY() {
        return mouseDeltaY;
    }

    public boolean isKeyDown(Key key) {
        return glfwGetKey(window.getHandle(), key.getCode()) == GLFW_PRESS;
    }

    public boolean isKeyUp(Key key) {
        return glfwGetKey(window.getHandle(), key.getCode()) == GLFW_RELEASE;
    }
}
