package com.crystal.engine.core;

import com.crystal.engine.render.Renderer;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.window.Window;

public class EngineContext {

    private final Window window;
    private final Renderer renderer;
    private final ResourceManager resources;
    private final Scene scene;

    public EngineContext(Window window, Renderer renderer, ResourceManager resources, Scene scene) {
        this.window = window;
        this.renderer = renderer;
        this.resources = resources;
        this.scene = scene;
    }

    // READ-ONLY ACCESS

    public Window getWindow() {
        return window;
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
