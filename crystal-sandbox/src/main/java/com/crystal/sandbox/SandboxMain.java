package com.crystal.sandbox;

import com.crystal.engine.core.Engine;
import com.crystal.engine.core.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SandboxMain implements Game {

    private static final Logger logger =
            LoggerFactory.getLogger(SandboxMain.class);

    public static void main(String[] args) {
        logger.info("Sandbox starting");

        Engine engine = new Engine(new SandboxMain());
        engine.run();

        logger.info("Sandbox exiting");
    }

    @Override
    public void init() {
        logger.info("Game init");
    }

    @Override
    public void update(double dt) {
        // game logic
    }

    @Override
    public void render() {
        // render logic
    }

    @Override
    public void shutdown() {
        logger.info("Game shutdown");
    }
}