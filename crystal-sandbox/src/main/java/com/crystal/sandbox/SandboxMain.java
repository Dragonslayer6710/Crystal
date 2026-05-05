package com.crystal.sandbox;

import com.crystal.engine.core.EngineContext;
import com.crystal.engine.input.Key;
import com.crystal.engine.input.MouseButton;
import com.crystal.engine.render.api.PrimitiveType;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.VertexLayout;
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

    @Override
    public void init(EngineContext ctx) {
        this.ctx = ctx;
        logger.info("Game init");

        float[] vertices = {
                // FRONT (+Z)
              // position              color    uv    normal
                -0.5f,  0.5f,  0.5f,   1,0,0,   0,1,  0,0,1,
                -0.5f, -0.5f,  0.5f,   1,0,0,   0,0,  0,0,1,
                 0.5f, -0.5f,  0.5f,   1,0,0,   1,0,  0,0,1,
                 0.5f,  0.5f,  0.5f,   1,0,0,   1,1,  0,0,1,

                // RIGHT (+X)
                 0.5f,  0.5f,  0.5f,   0,1,0,   0,1,  1,0,0,
                 0.5f, -0.5f,  0.5f,   0,1,0,   0,0,  1,0,0,
                 0.5f, -0.5f, -0.5f,   0,1,0,   1,0,  1,0,0,
                 0.5f,  0.5f, -0.5f,   0,1,0,   1,1,  1,0,0,

                // BACK (-Z)
                 0.5f,  0.5f, -0.5f,   0,0,1,   0,1,  0,0,-1,
                 0.5f, -0.5f, -0.5f,   0,0,1,   0,0,  0,0,-1,
                -0.5f, -0.5f, -0.5f,   0,0,1,   1,0,  0,0,-1,
                -0.5f,  0.5f, -0.5f,   0,0,1,   1,1,  0,0,-1,

                // LEFT (-X)
                -0.5f,  0.5f, -0.5f,   1,1,0,   0,1,  -1,0,0,
                -0.5f, -0.5f, -0.5f,   1,1,0,   0,0,  -1,0,0,
                -0.5f, -0.5f,  0.5f,   1,1,0,   1,0,  -1,0,0,
                -0.5f,  0.5f,  0.5f,   1,1,0,   1,1,  -1,0,0,

                // TOP (+Y)
                -0.5f,  0.5f, -0.5f,   1,0,1,   0,1,  0,1,0,
                -0.5f,  0.5f,  0.5f,   1,0,1,   0,0,  0,1,0,
                 0.5f,  0.5f,  0.5f,   1,0,1,   1,0,  0,1,0,
                 0.5f,  0.5f, -0.5f,   1,0,1,   1,1,  0,1,0,

                // BOTTOM (-Y)
                -0.5f, -0.5f,  0.5f,   0,1,1,   0,1,  0,-1,0,
                -0.5f, -0.5f, -0.5f,   0,1,1,   0,0,  0,-1,0,
                 0.5f, -0.5f, -0.5f,   0,1,1,   1,0,  0,-1,0,
                 0.5f, -0.5f,  0.5f,   0,1,1,   1,1,  0,-1,0,
        };

        int[] indices = {
                 0, 1, 2,  2, 3, 0,       // front
                 4, 5, 6,  6, 7, 4,       // right
                 8, 9,10, 10,11, 8,       // back
                12,13,14, 14,15,12,       // left
                16,17,18, 18,19,16,       // top
                20,21,22, 22,23,20        // bottom
        };

        Mesh mesh = this.ctx.getResources().createMesh(
                PrimitiveType.TRIANGLES,
                vertices,
                indices,
                VertexLayout.POSITION_COLOR_UV_NORMAL
        );

        ShaderProgram shaderProgram = this.ctx.getResources()
                .createShaderProgram("basic");

        Material material = new Material(shaderProgram);

        Texture texture = ctx.getResources().createTexture("test.png");
        material.setAlbedo(texture);

        Renderable renderable = new Renderable(mesh, material, new Transform().setPosition(0, 0, -2f));

        this.ctx.getScene().add(renderable);
    }

    private final boolean flying = false;

    private void move(double dt) {
        var input = ctx.getInput();
        var camera = ctx.getSceneCamera();
        var position = camera.getTransform().getPosition();
        
        float speed = ((input.isKeyDown(Key.LEFT_SHIFT)) ? 2f : 1f) * (float) dt;

        var forward = (flying) ? camera.getForward() : camera.getForwardXZ();
        var right = (flying) ? camera.getRight() :camera.getRightXZ();

        Vector3f movement = new Vector3f();

        if (input.isKeyDown(Key.W) && !input.isKeyDown(Key.S)) {
            movement.add(forward);
        } else if (input.isKeyDown(Key.S)) {
            movement.sub(forward);
        }

        if (input.isKeyDown(Key.D) && !input.isKeyDown(Key.A)) {
            movement.add(right);
        } else if (input.isKeyDown(Key.A)) {
            movement.sub(right);
        }

        if (input.isKeyDown(Key.SPACE) && !input.isKeyDown(Key.LEFT_CTRL)) {
            movement.y += 1.0f;
        } else if (input.isKeyDown(Key.LEFT_CTRL)) {
            movement.y -= 1.0f;
        }

        if (movement.lengthSquared() > 0.0f) {
            movement.normalize();
            movement.mul(speed);
            position.add(movement);
        }
    }
    
    private void look() {
        var input = ctx.getInput();
        var camera = ctx.getSceneCamera();
        var cameraTransform = camera.getTransform();

        float mouseSensitivity = 0.002f;

        var rotation = cameraTransform.getRotation();

        rotation.y -= input.getMouseDeltaX() * mouseSensitivity;
        rotation.x -= input.getMouseDeltaY() * mouseSensitivity;

        // clamp pitch to not flip upside down
        float maxPitch = (float) Math.toRadians(89.0f);

        if (rotation.x > maxPitch) {
            rotation.x = maxPitch;
        }

        if (rotation.x < -maxPitch) {
            rotation.x = -maxPitch;
        }
    }

    private boolean cursorCaptured = false;

    @Override
    public void update(double dt) {
        var input = ctx.getInput();
        var window = ctx.getWindow();

        // toggle capture
        if (input.isKeyPressed(Key.ESCAPE)) {
            cursorCaptured = false;
            window.setCursorCaptured(false);
        }

        if (input.isMousePressed(MouseButton.LMB)) {
            cursorCaptured = true;
            window.setCursorCaptured(true);
        }

        if (cursorCaptured) {
            move(dt);
            look();
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