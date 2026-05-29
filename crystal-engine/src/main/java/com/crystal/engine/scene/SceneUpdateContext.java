package com.crystal.engine.scene;

import com.crystal.engine.input.Input;
import com.crystal.engine.window.Window;

public class SceneUpdateContext {

    private final double deltaTime;
    private final Input input;
    private final Window window;
    private final Scene scene;

    public SceneUpdateContext(double deltaTime, Input input, Window window, Scene scene) {
        if (input == null) throw new IllegalArgumentException("Input cannot be null");
        if (window == null) throw new IllegalArgumentException("Window cannot be null");
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");

        this.deltaTime = deltaTime;
        this.input = input;
        this.window = window;
        this.scene = scene;
    }

    public double getDeltaTime() {
        return deltaTime;
    }

    public Input getInput() {
        return input;
    }

    public Window getWindow() {
        return window;
    }

    public Scene getScene() {
        return scene;
    }
}
