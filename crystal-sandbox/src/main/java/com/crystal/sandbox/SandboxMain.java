package com.crystal.sandbox;

import com.crystal.engine.core.EngineContext;
import com.crystal.engine.render.api.PrimitiveType;
import com.crystal.engine.render.commands.ClearCommand;
import com.crystal.engine.render.commands.DrawMeshCommand;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.shader.Shader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crystal.engine.core.Engine;
import com.crystal.engine.core.Game;

public class SandboxMain implements Game {

    private static final Logger logger =
            LoggerFactory.getLogger(SandboxMain.class);

    private EngineContext ctx;

    private float[] vertices;

    private Mesh mesh;

    private Shader shader;

    @Override
    public void init(EngineContext ctx) {
        logger.info("Game init");
        this.ctx = ctx;

        vertices = new float[]{
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        };
        mesh = new Mesh(PrimitiveType.TRIANGLES, vertices);
        shader  = new Shader(
                """
                #version 330 core
                layout(location = 0) in vec3 position;
                void main() {
                    gl_Position = vec4(position, 1.0);
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
    }

    @Override
    public void update(double dt) {
        // input + game logic later
    }

    @Override
    public void render() {
        ctx.getRenderer().submit(
                new ClearCommand(0.1f, 0.1f, 0.15f, 1f)
        );

        ctx.getRenderer().submit(
                new DrawMeshCommand(mesh, shader)
        );
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