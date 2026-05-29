package com.crystal.sandbox;

import com.crystal.engine.audio.SoundBuffer;
import com.crystal.engine.audio.SoundSource;
import com.crystal.engine.core.Engine;
import com.crystal.engine.core.EngineConfig;
import com.crystal.engine.core.EngineContext;
import com.crystal.engine.core.Game;
import com.crystal.engine.input.Key;
import com.crystal.engine.input.MouseButton;
import com.crystal.engine.input.action.InputAction;
import com.crystal.engine.input.action.InputMap;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.scene.Transform;
import com.crystal.engine.scene.component.CameraComponent;
import com.crystal.engine.scene.component.CameraLookComponent;
import com.crystal.engine.scene.component.CharacterControllerComponent;
import com.crystal.engine.scene.io.SceneLoader;
import com.crystal.engine.scene.io.SceneWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SandboxMain implements Game {

    private static final Logger logger =
        LoggerFactory.getLogger(SandboxMain.class);

    private static final Path DEMO_SCENE_PATH = Path.of("assets/scenes/demo_scene.json");
    private static final Path EXPORTED_SCENE_PATH = Path.of("assets/scenes/exported_scene.json");

    private static final boolean AUDIO_ENABLED = false;

    private static final float PLAYER_MOVE_SPEED = 1.0f;
    private static final float PLAYER_SPRINT_MULTIPLIER = 2.0f;

    private static final float PLAYER_HALF_WIDTH = 0.25f;
    private static final float PLAYER_HALF_HEIGHT = 0.75f;
    private static final float PLAYER_HALF_DEPTH = 0.25f;

    private static final float PLAYER_COLLIDER_CENTER_X = 0.0f;
    private static final float PLAYER_COLLIDER_CENTER_Y = -0.75f;
    private static final float PLAYER_COLLIDER_CENTER_Z = 0.0f;

    private final Set<String> activeTriggers = new HashSet<>();

    private final Set<String> collectedObjects = new HashSet<>();

    private final InputMap inputMap = new InputMap();

    private static final InputAction EXIT = new InputAction("exit");
    private static final InputAction CAPTURE_CURSOR = new InputAction("capture_cursor");
    private static final InputAction TOGGLE_FRUSTUM_CULLING = new InputAction("toggle_frustum_culling");
    private static final InputAction LOG_SCENE_DIAGNOSTICS = new InputAction("log_scene_diagnostics");
    private static final InputAction LOG_RENDERER_STATS = new InputAction("log_renderer_stats");
    private static final InputAction RELOAD_SCENE = new InputAction("reload_scene");
    private static final InputAction EXPORT_SCENE = new InputAction("export_scene");
    private static final InputAction PLAY_TEST_SOUND = new InputAction("play_test_sound");
    private static final InputAction CYCLE_DEBUG_VIEW = new InputAction("cycle_debug_view");

    private EngineContext ctx;

    private Shader sceneShader;

    private CharacterControllerComponent characterController;

    private SoundBuffer testSound;
    private SoundSource testSoundSource;

    private int collectibleCount;

    @Override
    public void init(EngineContext ctx) {
        this.ctx = ctx;
        logger.info("Game init");

        configureInput();

        sceneShader = this.ctx.getResources()
            .createShaderProgram("pbr");

        if (AUDIO_ENABLED) {
            testSound = ctx.getResources().loadSound("test.ogg");
            testSoundSource = ctx.getResources().manageResource(new SoundSource())
                .setBuffer(testSound);
        }

        reloadScene();
    }

    private void configureInput() {
        inputMap
            .bind(EXIT, Key.ESCAPE)
            .bind(CAPTURE_CURSOR, MouseButton.LMB)
            .bind(TOGGLE_FRUSTUM_CULLING, Key.F)
            .bind(LOG_SCENE_DIAGNOSTICS, Key.L)
            .bind(LOG_RENDERER_STATS, Key.P)
            .bind(RELOAD_SCENE, Key.R)
            .bind(EXPORT_SCENE, Key.O)
            .bind(PLAY_TEST_SOUND, Key.E)
            .bind(CYCLE_DEBUG_VIEW, Key.NUMPAD_ENTER);
    }

    private void reloadScene() {
        try {
            var loadedScene = SceneLoader.loadNew(
                DEMO_SCENE_PATH,
                ctx.getResources(),
                sceneShader
            );

            ctx.getScene().replaceWith(loadedScene.scene());

            activeTriggers.clear();
            collectedObjects.clear();

            addPlayerController();

            collectibleCount = ctx.getScene().findByTag("collectible").size();

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

    private void exportScene() {
        try {
            SceneWriter.write(EXPORTED_SCENE_PATH, ctx.getScene());

            var loadedScene = SceneLoader.loadNew(
                EXPORTED_SCENE_PATH,
                ctx.getResources(),
                sceneShader
            );

            logger.info("Exported and validated scene '{}'", EXPORTED_SCENE_PATH);
        } catch (RuntimeException e) {
            logger.error("Failed to export or validate scene '{}'", EXPORTED_SCENE_PATH, e);
        }
    }

    private void addPlayerController() {
        Transform controllerTransform = createPlayerControllerTransform();

        CameraComponent cameraComponent = new CameraComponent(ctx.getScene().getCamera());

        characterController = new CharacterControllerComponent()
            .setMoveSpeed(PLAYER_MOVE_SPEED)
            .setSprintMultiplier(PLAYER_SPRINT_MULTIPLIER)
            .setHalfExtents(PLAYER_HALF_WIDTH, PLAYER_HALF_HEIGHT, PLAYER_HALF_DEPTH)
            .setColliderCenterOffset(
                PLAYER_COLLIDER_CENTER_X,
                PLAYER_COLLIDER_CENTER_Y,
                PLAYER_COLLIDER_CENTER_Z
            );

        SceneObject cameraController = new SceneObject(
            "Player Controller",
            null,
            null,
            controllerTransform
        )
            .addComponent(new CameraLookComponent())
            .addComponent(characterController)
            .addComponent(cameraComponent);

        ctx.getScene().setActiveCamera(cameraComponent);
        ctx.getScene().add(cameraController);
    }

    private Transform createPlayerControllerTransform() {
        var camera = ctx.getScene().getCamera();
        var cameraTransform = camera.getTransform();
        var cameraPosition = cameraTransform.getPosition();

        return new Transform()
            .setPosition(cameraPosition.x, cameraPosition.y, cameraPosition.z)
            .setRotation(cameraTransform.getRotationQuat());
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

        if (inputMap.isPressed(input, EXIT)) {
            if (window.isCursorCaptured()) {
                window.setCursorCaptured(false);
            } else {
                ctx.getApplication().stop();
            }
        }

        if (!imguiWantsMouse && inputMap.isPressed(input, CAPTURE_CURSOR))
            window.setCursorCaptured(true);

        if (inputMap.isPressed(input, TOGGLE_FRUSTUM_CULLING))
            renderer.setFrustumCullingEnabled(!renderer.isFrustumCullingEnabled());

        if (inputMap.isPressed(input, LOG_SCENE_DIAGNOSTICS))
            logSceneDiagnostics();

        if (inputMap.isPressed(input, LOG_RENDERER_STATS))
            logger.info("Renderer stats: {}", renderer.getStats().summary());

        if (inputMap.isPressed(input, RELOAD_SCENE))
            reloadScene();

        if (inputMap.isPressed(input, EXPORT_SCENE))
            exportScene();

        if (AUDIO_ENABLED)
            if (inputMap.isPressed(input, PLAY_TEST_SOUND))
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

        if (inputMap.isPressed(input, CYCLE_DEBUG_VIEW))
            renderer.cycleDebugViewMode();
    }

    private List<SceneObject> currentTriggerIntersections() {
        if (characterController == null || characterController.getOwner() == null)
            return List.of();

        return ctx.getScene().findTriggersIntersecting(
            characterController.getCollider(),
            characterController.getOwner().getTransform()
        );
    }

    private void updateTriggerDiagnostics() {
        Set<String> currentTriggers = currentTriggerIntersections()
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

    private void updateCollectibles() {
        var collectibles = currentTriggerIntersections()
            .stream()
            .filter(object -> object.hasTag("collectible"))
            .toList();

        for (SceneObject collectible : collectibles) {
            String name = collectible.getName();

            if (!collectedObjects.add(name))
                continue;

            collectible
                .setVisible(false)
                .setActive(false);

            logger.info(
                "Collected '{}' ({}/{})",
                name,
                collectedObjects.size(),
                collectibleCount
            );

            if (collectedObjects.size() == collectibleCount)
                logger.info("All collectibles collected");
        }
    }

    @Override
    public void update(double dt) {
        handleInput();
        updateTriggerDiagnostics();
        updateCollectibles();
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
