package com.crystal.engine.core;

import com.crystal.engine.input.Input;
import com.crystal.engine.render.Renderer;
import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.window.Window;

public class EngineContext {

    private final Window window;
    private final Input input;
    private final Renderer renderer;
    private final ResourceManager resources;
    private final Scene scene;

    public EngineContext(Window window, Input input, Renderer renderer, ResourceManager resources, Scene scene) {
        this.window = window;
        this.input = input;
        this.renderer = renderer;
        this.resources = resources;
        this.scene = scene;
    }

    // READ-ONLY ACCESS

    public Window getWindow() {
        return window;
    }

    public Input getInput() {
        return input;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public ResourceManager getResources() {
        return resources;
    }

    public Scene getScene() {
        return scene;
    }
}
