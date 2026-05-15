package com.crystal.sandbox;

import com.crystal.engine.assets.model.Model;
import com.crystal.engine.assets.model.ModelLoadOptions;
import com.crystal.engine.core.EngineConfig;
import com.crystal.engine.core.EngineContext;
import com.crystal.engine.input.Key;
import com.crystal.engine.render.environment.IBLGenerator;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.MeshFactory;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Transform;
import com.crystal.engine.render.shader.Shader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crystal.engine.core.Engine;
import com.crystal.engine.core.Game;

public class SandboxMain implements Game {

    private static final Logger logger =
            LoggerFactory.getLogger(SandboxMain.class);

    private static final boolean ENABLE_IBL = true;

    private static final boolean SHOW_CUBES = true;
    private static final boolean SHOW_HELMET = true;
    private static final boolean SHOW_FLOOR = true;

    private EngineContext ctx;

    private SceneObject cubeA;
    private SceneObject cubeB;
    private SceneObject cubeC;

    private SceneObject boxTextured;

    private SceneObject helmet;

    private FlyCameraController cameraController;

    private void addCubes(Shader shader) {
        Mesh mesh = MeshFactory.createLitTexturedCube(ctx.getResources());

        Material material = new Material(shader);
        material.getRenderState()
                .setWireframe(false)
                .setCullFace(true);

        material.setAlbedo(ctx.getResources().createTexture("bricks_albedo.png"));
        material.setNormalMap(ctx.getResources().createDataTexture("bricks_normal.png"));

        cubeA = new SceneObject("Cube A", mesh, material, new Transform().setPosition(-2, 0, -2f));
        cubeB = new SceneObject("Cube B", mesh, material, new Transform().setPosition(0, 0, -2f));
        cubeC = new SceneObject("Cube C", mesh, material, new Transform().setPosition(2, 0, -2f));

        cubeA.addChild(cubeB);
        cubeB.addChild(cubeC);
        this.ctx.getScene().add(cubeA);


        Model cubeModel = ctx.getResources().loadModel(
                "/external/BoxTextured.glb",
                new ModelLoadOptions().setShader(shader)
        );
        cubeModel.logHierarchy();
        boxTextured = cubeModel.getRootObjects().getFirst();

        boxTextured.getTransform().setPosition(2, 0, -1f);

        this.ctx.getScene().add(boxTextured);
    }

    private void addHelmet(Shader shader) {
        Model damagedHelmetModel = ctx.getResources().loadModel(
                "/external/DamagedHelmet.glb",
                new ModelLoadOptions().setShader(shader)
        );
        damagedHelmetModel.logHierarchy();
        helmet = damagedHelmetModel.getRootObjects().getFirst();

        ctx.getScene().add(helmet);

        helmet.getTransform()
                .setPosition(0.75f, 1.0f, -2.5f)
                .setRotationDegrees(90.0f, 0.0f, -15.0f);

        helmet.setCastsShadowRecursive(false);
    }

    private void addFloor(Shader shader) {
        Mesh floorMesh = MeshFactory.createLitTexturedPlane(ctx.getResources());

        Material floorMaterial = new Material(shader);
        floorMaterial.setAlbedo(ctx.getResources().createTexture("bricks_albedo.png"));
        floorMaterial.setNormalMap(ctx.getResources().createDataTexture("bricks_normal.png"));

        SceneObject floor = new SceneObject(
                "Floor",
                floorMesh,
                floorMaterial,
                new Transform()
                        .setPosition(0.0f, -1.0f, -2.5f)
                        .setScale(8.0f, 1.0f, 8.0f)
        );

        ctx.getScene().add(floor);
    }

    @Override
    public void init(EngineContext ctx) {
        this.ctx = ctx;
        logger.info("Game init");

        Shader shader = this.ctx.getResources()
                .createShaderProgram("pbr");

        if (SHOW_CUBES) {
            addCubes(shader);
        }

        if (SHOW_HELMET) {
            addHelmet(shader);
        }

        if (SHOW_FLOOR) {
            addFloor(shader);
        }

        if (ENABLE_IBL) {
            IBLGenerator iblGenerator = IBLGenerator.createDefault(ctx.getResources());
            iblGenerator.generateFromHDR(
                    ctx.getScene().getEnvironment(),
                    "environment/studio_small_03_1k.hdr"
            );
        }

        ctx.getScene().getDirectionalLight()
                .setIntensity(3.0f)
                .setShadowStrength(1.0f);

        ctx.getScene().getEnvironment()
                .setAmbientColor(0.01f, 0.01f, 0.01f)
                .setAmbientIntensity(0.2f)
                .setIblIntensity(0.4f);

        cameraController = new FlyCameraController(ctx);

        ctx.getScene().getCamera().getTransform().translate(0, 0, 2.0f);
    }

    @Override
    public void update(double dt) {
        cameraController.update(dt);

        var input = ctx.getInput();
        var renderer = ctx.getRenderer();

        if (input.isKeyPressed(Key.F))
            renderer.setFrustumCullingEnabled(!renderer.isFrustumCullingEnabled());

        if (input.isKeyPressed(Key.P))
            logger.info("Renderer stats: {}", renderer.getStats().summary());

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

        if (SHOW_CUBES){
            cubeA.getTransform().rotate(0.0f, (float) dt, 0.0f);
            cubeB.getTransform().rotate((float) dt, 0.0f, 0.0f);
            cubeC.getTransform().rotate(0.0f, 0.0f, (float) dt);

            boxTextured.getTransform().rotate(0.0f, (float) dt, 0.0f);
        }

        if (SHOW_HELMET) {
            helmet.getTransform().rotate(0.0f, 0.0f, (float) dt);
        }

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
