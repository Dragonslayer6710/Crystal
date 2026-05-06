package com.crystal.sandbox;

import com.crystal.engine.core.EngineContext;
import com.crystal.engine.input.Key;
import com.crystal.engine.input.MouseButton;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.MeshFactory;
import com.crystal.engine.render.scene.Renderable;
import com.crystal.engine.render.scene.Transform;
import com.crystal.engine.render.shader.ShaderProgram;
import com.crystal.engine.render.texture.Texture;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crystal.engine.core.Engine;
import com.crystal.engine.core.Game;

public class SandboxMain implements Game {

    private static final Logger logger =
            LoggerFactory.getLogger(SandboxMain.class);

    private EngineContext ctx;

    private Renderable cubeA;
    private Renderable cubeB;
    private Renderable cubeC;

    private FlyCameraController cameraController;

    @Override
    public void init(EngineContext ctx) {
        this.ctx = ctx;
        logger.info("Game init");

        Mesh mesh = MeshFactory.createTexturedCube(ctx.getResources());

        ShaderProgram shaderProgram = this.ctx.getResources()
                .createShaderProgram("basic");

        Material material = new Material(shaderProgram);

        Texture texture = ctx.getResources().createTexture("test.png");
        material.setAlbedo(texture);

        cubeA = new Renderable(mesh, material, new Transform().setPosition(-2, 0, -2f));
        cubeB = new Renderable(mesh, material, new Transform().setPosition( 0, 0, -2f));
        cubeC = new Renderable(mesh, material, new Transform().setPosition( 2, 0, -2f));

        this.ctx.getScene().add(cubeA);
        this.ctx.getScene().add(cubeB);
        this.ctx.getScene().add(cubeC);

        cameraController = new FlyCameraController(ctx);
    }

    @Override
    public void update(double dt) {
        cameraController.update(dt);

        cubeA.getTransform().rotate(0.0f, (float) dt, 0.0f);
        cubeB.getTransform().rotate((float) dt, 0.0f, 0.0f);
        cubeC.getTransform().rotate(0.0f, 0.0f, (float) dt);
    }

    @Override
    public void shutdown() {
        logger.info("Game shutdown");
    }


    public static void main(String[] args) {
        logger.info("Sandbox starting");

        Engine engine = new Engine(new SandboxMain());
        engine.run();

        logger.info("Sandbox exiting");
    }
}