package com.crystal.engine.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine {

    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    // FPS Limit
    private static final int targetFPS = 144;
    private static final long targetTime = 1_000_000_000 / targetFPS;
    private boolean limitFPS = true;

    private boolean running = false;


    private Game game;

    public Engine(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }

        this.game = game;
    }

    private void init() {
        logger.info("Engine initialising");
        running = true;
        game.init();
    }

    private void update(double deltaTime) {
        game.update(deltaTime);
    }

    private void render() {
        game.render();
    }

    private void shutdown() {
        game.shutdown();
        logger.info("Engine Shutting down");
    }

    public void run() {
        init();

        long lastTime = System.nanoTime();

        // TODO: remove this eventually
        long frames = 0;

        while (running) {
            long start = System.nanoTime();

            long now = System.nanoTime();
            double deltaTime = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            update(deltaTime);
            render();

            if (limitFPS) {
                long frameTime = System.nanoTime() - start;
                long sleepTime = targetTime - frameTime;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(Math.max(0, sleepTime / 1_000_000));
                    } catch (InterruptedException ignored) {}
                }
            }

            // TEMP: stop after 10 seconds to see it working
            if (++frames > 10 * targetFPS) running = false;
        }

        shutdown();
    }
}
