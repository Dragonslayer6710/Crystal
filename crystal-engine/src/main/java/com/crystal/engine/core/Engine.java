package com.crystal.engine.core;

import com.crystal.engine.input.Input;
import com.crystal.engine.render.Renderer;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowEventListener;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine implements WindowEventListener {

    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private final EngineConfig config;

    private final Game game;

    private Window window;
    private Input input;
    private Renderer renderer;
    private ResourceManager resourceManager;
    private Scene scene;

    private EngineContext context;

    private boolean running;

    private final long targetFrameTime;

    public Engine(Game game, EngineConfig config) {
        if (game == null) throw new IllegalArgumentException("Game cannot be null");
        if (config == null) throw new IllegalArgumentException("EngineConfig cannot be null");

        this.game = game;
        this.config = config;

        targetFrameTime = 1_000_000_000 / config.getTargetFPS();
    }

    public Engine(Game game) {
        this(game, new EngineConfig());
    }

    private void init() {
        logger.info("Engine initialising");

        input = new Input();

        window = new Window(config.getWidth(), config.getHeight(), config.getTitle());
        window.create();

        window.setWindowEventListener(this);
        window.setInputListener(input);

        GL.createCapabilities();

        renderer = new Renderer();
        renderer.init(config.getWidth(), config.getHeight());

        resourceManager = new ResourceManager();

        scene = new Scene();

        context = new EngineContext(window, input, renderer, resourceManager, scene);

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

            // 1. INPUT START
            context.getInput().beginFrame();

            // 2. POLL EVENTS FROM WINDOW
            window.pollEvents();

            // 3 GAME LOGIC
            game.update(dt);

            // 4. INPUT END
            context.getInput().endFrame();

            // 5. RENDERER RENDERS SCENE
            renderer.render(context.getScene(), window.getAspectRatio());

            // 6. PRESENT FRAME (WINDOW RESPONSIBILITY)
            window.swapBuffers();

            throttle(frameStart);
        }

        shutdown();
    }

    private void throttle(long frameStart) {
        long elapsed = System.nanoTime() - frameStart;
        long sleepNanos = targetFrameTime - elapsed;

        if (sleepNanos > 0) {
            try {
                Thread.sleep(sleepNanos / 1_000_000);
            } catch (InterruptedException ignored) {}
        }
    }

    private void shutdown() {
        logger.info("Engine shutting down");

        game.shutdown();
        scene.dispose();
        resourceManager.disposeAll();
        window.destroy();
    }

    @Override
    public void onFrameBufferResize(int width, int height) {
        renderer.resizeViewport(width, height);
    }
}