package com.crystal.engine.core;

import com.crystal.engine.render.Renderer;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine {

    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private final Game game;

    private Window window;
    private Renderer renderer;
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

        GL.createCapabilities();

        renderer = new Renderer();
        renderer.init(1280, 720);

        context = new EngineContext(window, renderer);

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

            // 2. START FRAME RENDER
            renderer.beginFrame();

            // 3. GAME SUBMITS DRAW COMMANDS
            game.render();

            // 4. EXECUTE RENDER COMMANDS
            renderer.renderFrame();

            // 5. PRESENT FRAME (WINDOW RESPONSIBILITY)
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
        window.destroy();
    }
}