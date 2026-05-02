package com.crystal.engine.core;

import com.crystal.engine.render.Renderer;

public class EngineContext {

    private final Window window;
    private final Renderer renderer;

    public EngineContext(Window window, Renderer renderer) {
        this.window = window;
        this.renderer = renderer;
    }

    // READ-ONLY ACCESS

    public Window getWindow() {
        return window;
    }

    public Renderer getRenderer() {
        return renderer;
    }
}
