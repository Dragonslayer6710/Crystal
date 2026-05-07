package com.crystal.engine.core;

import com.crystal.engine.input.Input;
import com.crystal.engine.render.GLDebug;
import com.crystal.engine.render.Renderer;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowEventListener;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.LockSupport;

public class Engine implements WindowEventListener, Application {

    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private final Game game;
    private final EngineConfig config;

    private Time time;
    private Input input;
    private Window window;
    private Renderer renderer;
    private ResourceManager resourceManager;
    private Scene scene;

    private EngineContext context;

    private boolean running;
    private boolean shutdown;

    public Engine(Game game, EngineConfig config) {
        if (game == null) throw new IllegalArgumentException("Game cannot be null");
        if (config == null) throw new IllegalArgumentException("EngineConfig cannot be null");

        this.game = game;
        this.config = config;
    }

    public Engine(Game game) {
        this(game, new EngineConfig());
    }

    private void init() {
        logger.info("Engine initialising");

        time = new Time();

        input = new Input();

        var windowConfig = config.getWindowConfig();

        window = new Window(windowConfig);
        window.create();

        window.setWindowEventListener(this);
        window.setInputListener(input);

        GL.createCapabilities();
        if (windowConfig.isDebugContext())
            GLDebug.init();

        renderer = new Renderer(config.getRendererConfig());
        renderer.init(windowConfig.getWidth(), windowConfig.getHeight());

        resourceManager = new ResourceManager();

        scene = new Scene();

        context = new EngineContext(
                this,
                time,
                input,
                window,
                renderer,
                resourceManager,
                scene
        );

        game.init(context);

        running = true;
    }

    public void run() {
        try {
            init();

            long lastTime = System.nanoTime();

            while (running && !window.shouldClose()) {
                long frameStart = System.nanoTime();

                time.update(frameStart, lastTime, config.getMaxDeltaTime());
                lastTime = frameStart;

                // 1. INPUT START
                context.getInput().beginFrame();

                // 2. POLL EVENTS FROM WINDOW
                window.pollEvents();

                // 3 GAME LOGIC
                game.update(time.getDeltaTime());

                // 4. INPUT END
                context.getInput().endFrame();

                // 5. RENDERER RENDERS SCENE
                renderer.render(context.getScene(), window.getAspectRatio());

                // 6. PRESENT FRAME (WINDOW RESPONSIBILITY)
                window.swapBuffers();

                // 7. THROTTLE FPS IF config.targetFPS > 0
                if (config.getTargetFPS() > 0)
                    throttle(frameStart);
            }
        } finally {
            shutdown();
        }
    }

    private void throttle(long frameStart) {
        long elapsed = System.nanoTime() - frameStart;
        long sleepNanos = config.getTargetFrameTimeNanos() - elapsed;

        if (sleepNanos <= 0)
            return;

        LockSupport.parkNanos(sleepNanos);

        if (Thread.interrupted())
            Thread.currentThread().interrupt();
    }

    private void shutdown() {
        if (shutdown)
            return;

        shutdown = true;
        running = false;

        logger.info("Engine shutting down");

        try {
            game.shutdown();
        } catch (Exception e) {
            logger.error("Game Shutdown failed", e);
        }

        if (scene != null) scene.dispose();

        if (resourceManager != null) resourceManager.disposeAll();

        GLDebug.dispose();

        if (window != null) window.destroy();
    }

    @Override
    public void onFrameBufferResize(int width, int height) {
        if (renderer != null) renderer.resizeViewport(width, height);
    }

    @Override
    public void stop() {
        running = false;
    }
}