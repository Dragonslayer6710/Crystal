package com.crystal.engine.input;

public interface InputListener {
    void onKey(Key key, boolean pressed);
    void onMouse(MouseButton button, boolean pressed);
    void onMouseMove(double x, double y);
}
