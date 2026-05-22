package com.crystal.sandbox;

import com.crystal.engine.audio.SoundBuffer;
import com.crystal.engine.audio.SoundSource;
import com.crystal.engine.core.EngineConfig;
import com.crystal.engine.core.EngineContext;
import com.crystal.engine.input.Key;
import com.crystal.engine.input.MouseButton;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.render.scene.SceneLoader;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Transform;
import com.crystal.engine.render.shader.Shader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crystal.engine.core.Engine;
import com.crystal.engine.core.Game;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SandboxMain implements Game {

    private static final Logger logger =
            LoggerFactory.getLogger(SandboxMain.class);

    private static final Path DEMO_SCENE_PATH = Path.of("assets/scenes/demo_scene.json");
    private static final boolean AUDIO_ENABLED = false;

    private final Set<String> activeTriggers = new HashSet<>();

    private EngineContext ctx;

    private Shader sceneShader;

    private SoundBuffer testSound;
    private SoundSource testSoundSource;

    @Override
    public void init(EngineContext ctx) {
        this.ctx = ctx;
        logger.info("Game init");

        sceneShader = this.ctx.getResources()
                .createShaderProgram("pbr");

        if (AUDIO_ENABLED) {
            testSound = ctx.getResources().loadSound("test.ogg");
            testSoundSource = ctx.getResources().manageResource(new SoundSource())
                .setBuffer(testSound);
        }

        reloadScene();
    }

    private void reloadScene() {
        try {
            var loadedScene = SceneLoader.loadNew(
                DEMO_SCENE_PATH,
                ctx.getResources(),
                sceneShader
            );

            ctx.getScene().replaceWith(loadedScene.scene());
            loadedScene.scene().dispose();

            addCameraController();

            activeTriggers.clear();

            logger.info(
                "Reloaded scene '{}' v{} from '{}'",
                loadedScene.name(),
                loadedScene.version(),
                DEMO_SCENE_PATH
            );
        } catch (RuntimeException e) {
            logger.error("Failed to reload scene '{}'; keeping current scene", DEMO_SCENE_PATH, e);
        }
    }

    private void addCameraController() {
        SceneObject cameraController = new SceneObject("Camera Controller", null, null, new Transform())
            .addComponent(new FlyCameraController(ctx.getScene().getCamera())
                .setMoveSpeed(1.0f)
                .setSprintMultiplier(2.0f)
                .setFlying(false));

        ctx.getScene().add(cameraController);
    }

    private void logSceneDiagnostics() {
        var scene = ctx.getScene();

        logger.info(
            "Scene diagnostics: roots={}, models={}, rotating={}, debug={}, environment={}",
            scene.getRootObjects().size(),
            scene.findByTag("model").size(),
            scene.findByTag("rotating").size(),
            scene.findByTag("debug").size(),
            scene.findByTag("environment").size()
        );
    }

    private void handleInput() {
        var input = ctx.getInput();
        var renderer = ctx.getRenderer();
        var window = ctx.getWindow();

        boolean imguiWantsMouse = ctx.getDebugOverlay().wantsMouse();

        if (input.isKeyPressed(Key.ESCAPE)) {
            if (window.isCursorCaptured()) {
                window.setCursorCaptured(false);
            } else {
                ctx.getApplication().stop();
            }
        }

        if (!imguiWantsMouse && input.isMousePressed(MouseButton.LMB))
            window.setCursorCaptured(true);

        if (input.isKeyPressed(Key.F))
            renderer.setFrustumCullingEnabled(!renderer.isFrustumCullingEnabled());

        if (input.isKeyPressed(Key.L))
            logSceneDiagnostics();

        if (input.isKeyPressed(Key.P))
            logger.info("Renderer stats: {}", renderer.getStats().summary());

        if (input.isKeyPressed(Key.R))
            reloadScene();

        if (AUDIO_ENABLED)
            if (input.isKeyPressed(Key.E))
                testSoundSource.play();

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

    private void updateTriggerDiagnostics() {
        var cameraPosition = ctx.getScene().getCamera().getTransform().getWorldPosition();

        Set<String> currentTriggers = ctx.getScene()
            .findTriggersContaining(cameraPosition)
            .stream()
            .map(SceneObject::getName)
            .collect(Collectors.toSet());

        for (String trigger : currentTriggers) {
            if (!activeTriggers.contains(trigger))
                logger.info("Entered trigger '{}'", trigger);
        }

        for (String trigger : activeTriggers) {
            if (!currentTriggers.contains(trigger))
                logger.info("Exited trigger '{}'", trigger);
        }

        activeTriggers.clear();
        activeTriggers.addAll(currentTriggers);
    }

    @Override
    public void update(double dt) {
        handleInput();
        updateTriggerDiagnostics();
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
