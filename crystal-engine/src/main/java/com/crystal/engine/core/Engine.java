package com.crystal.engine.core;

import com.crystal.engine.render.Renderer;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowEventListener;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine implements WindowEventListener {

    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private final Game game;

    private Window window;
    private Renderer renderer;
    private ResourceManager resourceManager;
    private Scene scene;
    private EngineContext context;

    private boolean running;

    private static final int TARGET_FPS = 144;
    private static final long TARGET_FRAME_TIME = 1_000_000_000 / TARGET_FPS;

    public Engine(Game game) {
        if (game == null) throw new IllegalArgumentException("Game cannot be null");
        this.game = game;
    }

    private void init() {
        logger.info("Engine initialising");

        window = new Window(1280, 720, "Crystal Engine");
        window.create();
        window.setEventListener(this);

        GL.createCapabilities();

        renderer = new Renderer();
        renderer.init(1280, 720);

        resourceManager = new ResourceManager();

        scene = new Scene();

        context = new EngineContext(window, renderer, resourceManager, scene);

        game.init(context);

        running = true;
    }

    public void run() {
        init();

        long lastTime = System.nanoTime();

        while (running && !window.shouldClose()) {
            long frameStart = System.nanoTime();
            double dt = (frameStart - lastTime) / 1_000_000_000.0;
            lastTime = frameStart;

            // 1. INPUT / GAME LOGIC
            game.update(dt);

            // 2. Renderer Renders Scene
            renderer.render(context.getScene(), window.getAspectRatio());

            // 3. PRESENT FRAME (WINDOW RESPONSIBILITY)
            window.update();

            throttle(frameStart);
        }

        shutdown();
    }

    private void throttle(long frameStart) {
        long elapsed = System.nanoTime() - frameStart;
        long sleepNanos = TARGET_FRAME_TIME - elapsed;

        if (sleepNanos > 0) {
            try {
                Thread.sleep(sleepNanos / 1_000_000);
            } catch (InterruptedException ignored) {}
        }
    }

    private void shutdown() {
        logger.info("Engine shutting down");

        game.shutdown();
        resourceManager.disposeAll();
        window.destroy();
    }

    @Override
    public void onFrameBufferResize(int width, int height) {
        renderer.resizeViewport(width, height);
    }
}