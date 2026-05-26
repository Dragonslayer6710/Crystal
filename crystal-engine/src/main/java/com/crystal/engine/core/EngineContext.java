package com.crystal.engine.core;

import com.crystal.engine.assets.ResourceManager;
import com.crystal.engine.debug.DebugOverlay;
import com.crystal.engine.input.Input;
import com.crystal.engine.render.Renderer;
import com.crystal.engine.scene.Scene;
import com.crystal.engine.window.Window;

public class EngineContext {

    private final Application application;
    private final Time time;
    private final Input input;
    private final Window window;
    private final Renderer renderer;
    private final ResourceManager resources;
    private final Scene scene;

    private final DebugOverlay debugOverlay;

    public EngineContext(Application application, Time time, Input input, Window window,
                         Renderer renderer, ResourceManager resources, Scene scene, DebugOverlay debugOverlay) {
        this.application = application;
        this.time = time;
        this.input = input;
        this.window = window;
        this.renderer = renderer;
        this.resources = resources;
        this.scene = scene;
        this.debugOverlay = debugOverlay;
    }

    // READ-ONLY ACCESS
    public Application getApplication() { return application; }
    public Time getTime() { return time; }
    public Input getInput() { return input; }
    public Window getWindow() { return window; }
    public Renderer getRenderer() { return renderer; }
    public ResourceManager getResources() { return resources; }
    public Scene getScene() { return scene; }
    public DebugOverlay getDebugOverlay() { return debugOverlay; }
}
