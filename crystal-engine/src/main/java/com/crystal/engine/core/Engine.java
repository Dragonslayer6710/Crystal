package com.crystal.engine.core;

import com.crystal.engine.input.Input;
import com.crystal.engine.render.GLDebug;
import com.crystal.engine.render.Renderer;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.render.shader.ShaderException;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowEventListener;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private EngineState state = EngineState.CREATED;

    private double statsTitleTimer;

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
        state = EngineState.INITIALISING;

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

        resourceManager = new ResourceManager(config.getAssetConfig());

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

        state = EngineState.RUNNING;
    }

    public void run() {
        try {
            init();

            long lastTime = System.nanoTime();

            while (state == EngineState.RUNNING && !window.shouldClose()) {
                long frameStart = System.nanoTime();

                time.update(frameStart, lastTime, config.getMaxDeltaTime());
                lastTime = frameStart;

                // 1. INPUT START
                context.getInput().beginFrame();

                // 2. POLL EVENTS FROM WINDOW
                window.pollEvents();

                // 3 GAME LOGIC
                long updateStart = System.nanoTime();
                game.update(time.getDeltaTime());
                time.setUpdateTimeNanos(System.nanoTime() - updateStart);

                // 4. INPUT END
                context.getInput().endFrame();

                // 5. RENDERER RENDERS SCENE
                long renderStart = System.nanoTime();
                renderer.render(context.getScene(), window.getAspectRatio());
                time.setRenderTimeNanos(System.nanoTime() - renderStart);

                // 6. PRESENT FRAME (WINDOW RESPONSIBILITY)
                window.swapBuffers();

                // 7. THROTTLE FPS IF config.targetFPS > 0
                if (config.getTargetFPS() > 0)
                    throttle(frameStart);

                time.setFrameTimeNanos(System.nanoTime() - frameStart);

                statsTitleTimer += time.getDeltaTime();

                if (statsTitleTimer >= 0.25) {
                    statsTitleTimer = 0.0;

                    window.setTitle(String.format(
                            "%s | FPS: %d | Frame: %.2fms | Update: %.2fms | Render: %.2fms",
                            config.getWindowConfig().getTitle(),
                            time.getFps(),
                            time.getFrameTimeMs(),
                            time.getUpdateTimeMs(),
                            time.getRenderTimeMs()
                    ));
                }
            }
        } catch (ShaderException e) {
            logger.error(e.getMessage());
        } finally {
            shutdown();
        }
    }

    private void throttle(long frameStart) {
        long targetEndTime = frameStart + config.getTargetFrameTimeNanos();

        // Accurate but CPU-heavy. Replace with hybrid sleep/spin later if needed.
        while (System.nanoTime() < targetEndTime) {
            Thread.onSpinWait();
        }
    }

    private void shutdown() {
        if (state == EngineState.SHUTDOWN)
            return;

        state = EngineState.STOPPING;

        logger.info("Engine shutting down");

        try {
            game.shutdown();
        } catch (Exception e) {
            logger.error("Game Shutdown failed", e);
        }

        if (scene != null) scene.dispose();

        if (resourceManager != null) resourceManager.disposeAll();

        if (renderer != null) renderer.dispose();

        GLDebug.dispose();

        if (window != null) window.destroy();

        state = EngineState.SHUTDOWN;
    }

    @Override
    public void onFrameBufferResize(int width, int height) {
        if (renderer != null) renderer.resizeViewport(width, height);
    }

    @Override
    public void stop() {
        if (state == EngineState.RUNNING) state = EngineState.STOPPING;
    }
}