package com.crystal.sandbox;

import com.crystal.engine.core.EngineConfig;
import com.crystal.engine.core.EngineContext;
import com.crystal.engine.input.Key;
import com.crystal.engine.render.scene.SceneLoader;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Transform;
import com.crystal.engine.render.shader.Shader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crystal.engine.core.Engine;
import com.crystal.engine.core.Game;

import java.nio.file.Path;

public class SandboxMain implements Game {

    private static final Logger logger =
            LoggerFactory.getLogger(SandboxMain.class);

    private EngineContext ctx;

    private Shader sceneShader;
    private static final Path DEMO_SCENE_PATH = Path.of("assets/scenes/demo_scene.json");

    @Override
    public void init(EngineContext ctx) {
        this.ctx = ctx;
        logger.info("Game init");

        sceneShader = this.ctx.getResources()
                .createShaderProgram("pbr");

        reloadScene();
    }

    private void reloadScene() {
        ctx.getScene().clear();

        SceneLoader.load(
            DEMO_SCENE_PATH,
            ctx.getScene(),
            ctx.getResources(),
            sceneShader
        );

        addCameraController();
    }

    private void addCameraController() {
        SceneObject cameraController = new SceneObject("Camera Controller", null, null, new Transform())
            .addComponent(new FlyCameraController(ctx.getScene().getCamera(), ctx.getApplication())
                .setMoveSpeed(1.0f)
                .setSprintMultiplier(2.0f)
                .setFlying(false));

        ctx.getScene().add(cameraController);
    }

    @Override
    public void update(double dt) {
        var input = ctx.getInput();
        var renderer = ctx.getRenderer();

        if (input.isKeyPressed(Key.F))
            renderer.setFrustumCullingEnabled(!renderer.isFrustumCullingEnabled());

        if (input.isKeyPressed(Key.P))
            logger.info("Renderer stats: {}", renderer.getStats().summary());

        if (input.isKeyPressed(Key.R))
            reloadScene();

        if (input.isKeyPressed(Key.NUMPAD_0)) renderer.setDebugViewMode(0);
        if (input.isKeyPressed(Key.NUMPAD_1)) renderer.setDebugViewMode(1);
        if (input.isKeyPressed(Key.NUMPAD_2)) renderer.setDebugViewMode(2);
        if (input.isKeyPressed(Key.NUMPAD_3)) renderer.setDebugViewMode(3);
        if (input.isKeyPressed(Key.NUMPAD_4)) renderer.setDebugViewMode(4);
        if (input.isKeyPressed(Key.NUMPAD_5)) renderer.setDebugViewMode(5);
        if (input.isKeyPressed(Key.NUMPAD_6)) renderer.setDebugViewMode(6);
        if (input.isKeyPressed(Key.NUMPAD_7)) renderer.setDebugViewMode(7);
        if (input.isKeyPressed(Key.NUMPAD_8)) renderer.setDebugViewMode(8);
        if (input.isKeyPressed(Key.NUMPAD_9)) renderer.setDebugViewMode(9);

        if (input.isKeyPressed(Key.NUMPAD_ENTER))
            renderer.cycleDebugViewMode();

    }

    @Override
    public void shutdown() {
        logger.info("Game shutdown");
    }

    public static void main(String[] args) {
        logger.info("Sandbox starting");

        EngineConfig config = new EngineConfig();
        config.getAssetConfig().setAssetRoot("assets");

        Engine engine = new Engine(new SandboxMain(), config);
        engine.run();

        logger.info("Sandbox exiting");
    }
}
