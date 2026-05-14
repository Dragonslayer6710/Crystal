package com.crystal.sandbox;

import com.crystal.engine.core.EngineContext;
import com.crystal.engine.input.Key;
import com.crystal.engine.input.MouseButton;
import org.joml.Vector3f;

public class FlyCameraController {

    private final EngineContext ctx;

    private boolean cursorCaptured = false;
    private boolean flying = false;

    public FlyCameraController(EngineContext ctx) {
        this.ctx = ctx;
    }

    public void update(double dt) {
        handleCursorCapture();

        if (cursorCaptured) {
            move(dt);
            look();
        }
    }

    private void handleCursorCapture() {
        var input = ctx.getInput();
        var window = ctx.getWindow();

        // toggle capture
        if (input.isKeyPressed(Key.ESCAPE)) {
            if (cursorCaptured) {
                cursorCaptured = false;
                window.setCursorCaptured(false);
            } else {
                ctx.getApplication().stop();
            }
        }

        if (input.isMousePressed(MouseButton.LMB)) {
            cursorCaptured = true;
            window.setCursorCaptured(true);
        }
    }

    private void move(double dt) {
        var input = ctx.getInput();
        var camera = ctx.getScene().getCamera();

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
            camera.getTransform().translate(movement.x, movement.y, movement.z);
        }
    }

    private void look() {
        var input = ctx.getInput();
        var camera = ctx.getScene().getCamera();
        var cameraTransform = camera.getTransform();

        float mouseSensitivity = 0.002f;

        var rotation = cameraTransform.getRotation();

        rotation.y -= input.getMouseDeltaX() * mouseSensitivity;
        rotation.x -= input.getMouseDeltaY() * mouseSensitivity;

        // clamp pitch to not flip upside down
        float maxPitch = (float) Math.toRadians(89.0f);
        rotation.x = Math.max(-maxPitch, Math.min(maxPitch, rotation.x));

        cameraTransform.setRotation(rotation.x, rotation.y, rotation.z);
    }
}
