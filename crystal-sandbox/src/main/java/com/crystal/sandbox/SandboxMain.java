package com.crystal.sandbox;

import com.crystal.engine.core.EngineContext;
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

    @Override
    public void init(EngineContext ctx) {
        logger.info("Game init");

        Mesh mesh = ctx.getResources().createMesh(PrimitiveType.TRIANGLES, new float[]{
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        });

        ShaderProgram shaderProgram = ctx.getResources().createShaderProgram(
                """
                #version 330 core
                
                layout(location = 0) in vec3 position;
                
                uniform vec3 modelPos;
                
                void main() {
                    vec3 worldPos = position - modelPos;
                    gl_Position = vec4(worldPos, 1.0);
                }
                """,
                """
                #version 330 core
                out vec4 color;
                void main() {
                    color = vec4(1,1,1,1);
                }
                """
        );

        Material material = new Material(shaderProgram);

        Renderable renderable = new Renderable(mesh, material, new Transform(0.5f, 0.0f, 0.0f));

        ctx.getScene().add(renderable);
    }

    @Override
    public void update(double dt) {
        // input + game logic later
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