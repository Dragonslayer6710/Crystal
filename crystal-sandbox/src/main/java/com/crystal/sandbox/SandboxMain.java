package com.crystal.sandbox;

import com.crystal.engine.core.EngineContext;
import com.crystal.engine.input.Key;
import com.crystal.engine.render.api.PrimitiveType;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.scene.Renderable;
import com.crystal.engine.render.scene.Transform;
import com.crystal.engine.render.shader.ShaderProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crystal.engine.core.Engine;
import com.crystal.engine.core.Game;

public class SandboxMain implements Game {

    private static final Logger logger =
            LoggerFactory.getLogger(SandboxMain.class);

    private EngineContext ctx;

    @Override
    public void init(EngineContext ctx) {
        this.ctx = ctx;
        logger.info("Game init");

        Mesh mesh = this.ctx.getResources().createMesh(PrimitiveType.TRIANGLES, new float[]{
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        });

        ShaderProgram shaderProgram = this.ctx.getResources()
                .createShaderProgram("basic");

        Material material = new Material(shaderProgram);

        Renderable renderable = new Renderable(mesh, material, new Transform().setPosition(0, 0, -2f));

        this.ctx.getScene().add(renderable);
    }

    @Override
    public void update(double dt) {
        // input + game logic later
        var input = ctx.getInput();
        var camPos = ctx.getScene().getCamera().getTransform().getPosition();

        float speed = 2f * (float) dt;

        if (input.isKeyDown(Key.W)) {
            camPos.z -= speed;
        }

        if (input.isKeyDown(Key.A)) {
            camPos.x -= speed;
        }

        if (input.isKeyDown(Key.S)) {
            camPos.z += speed;
        }

        if (input.isKeyDown(Key.D)) {
            camPos.x += speed;
        }
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