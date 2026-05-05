package com.crystal.engine.input;

import com.crystal.engine.window.Window;

import java.util.EnumSet;

import static org.lwjgl.glfw.GLFW.*;

public class Input implements InputListener{

    private final EnumSet<Key> keysDown = EnumSet.noneOf(Key.class);
    private final EnumSet<Key> keysPressed = EnumSet.noneOf(Key.class);
    private final EnumSet<Key> keysReleased = EnumSet.noneOf(Key.class);

    // Mouse Input
    private double mouseX;
    private double mouseY;

    private double lastMouseX;
    private double lastMouseY;

    private double mouseDeltaX;
    private double mouseDeltaY;

    private boolean firstMouse = true;

    private final EnumSet<MouseButton> btnsDown = EnumSet.noneOf(MouseButton.class);
    private final EnumSet<MouseButton> btnsPressed = EnumSet.noneOf(MouseButton.class);
    private final EnumSet<MouseButton> btnsReleased = EnumSet.noneOf(MouseButton.class);

    public void update() {
        keysPressed.clear();
        keysReleased.clear();

        mouseDeltaX = mouseX - lastMouseX;
        mouseDeltaY = mouseY - lastMouseY;

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        btnsPressed.clear();
        btnsReleased.clear();
    }

    public boolean isKeyDown(Key key) {
        return keysDown.contains(key);
    }

    public boolean isKeyPressed(Key key) {
        return keysPressed.contains(key);
    }

    public boolean isKeyReleased(Key key) {
        return keysReleased.contains(key);
    }

    public double getMouseDeltaX() {
        return mouseDeltaX;
    }

    public double getMouseDeltaY() {
        return mouseDeltaY;
    }

    @Override
    public void onKey(Key key, boolean pressed) {
        if (pressed) {
            if (!keysDown.contains(key)) {
                keysPressed.add(key);
            }

            keysDown.add(key);
        } else {
            if (keysDown.contains(key)) {
                keysReleased.add(key);
            }

            keysDown.remove(key);
        }
    }

    @Override
    public void onMouseMove(double x, double y) {
        if (firstMouse) {
            mouseX = x;
            mouseY = y;
            lastMouseX = x;
            lastMouseY = y;
            firstMouse = false;
            return;
        }

        mouseX = x;
        mouseY = y;
    }

    @Override
    public void onMouseButton(MouseButton btn, boolean pressed) {
        if (pressed) {
            if (!btnsDown.contains(btn)) {
                btnsPressed.add(btn);
            }

            btnsDown.add(btn);
        } else {
            if (btnsDown.contains(btn)) {
                btnsReleased.add(btn);
            }

            btnsDown.remove(btn);
        }
    }
}
