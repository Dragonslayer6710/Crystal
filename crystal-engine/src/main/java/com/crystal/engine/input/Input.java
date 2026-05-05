package com.crystal.engine.input;

import java.util.EnumSet;

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

    public void beginFrame() {
        keysPressed.clear();
        keysReleased.clear();

        btnsPressed.clear();
        btnsReleased.clear();

        mouseDeltaX = 0.0;
        mouseDeltaY = 0.0;
    }

    public void endFrame() {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
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

    public boolean isMouseDown(MouseButton btn) {
        return btnsDown.contains(btn);
    }

    public boolean isMousePressed(MouseButton btn) {
        return btnsPressed.contains(btn);
    }

    public boolean isMouseReleased(MouseButton btn) {
        return btnsReleased.contains(btn);
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

        mouseDeltaX += mouseX - lastMouseX;
        mouseDeltaY += mouseY - lastMouseY;

        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    public void onMouse(MouseButton btn, boolean pressed) {
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
