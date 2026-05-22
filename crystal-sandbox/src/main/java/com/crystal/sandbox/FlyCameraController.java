package com.crystal.sandbox;

import com.crystal.engine.core.Application;
import com.crystal.engine.input.Key;
import com.crystal.engine.input.MouseButton;
import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.CameraControllerComponent;
import com.crystal.engine.render.scene.SceneUpdateContext;
import org.joml.Vector3f;

public class FlyCameraController extends CameraControllerComponent {

    private final Application application;

    private boolean cursorCaptured = false;

    private float moveSpeed = 1.0f;
    private float sprintMultiplier = 2.0f;
    private float mouseSensitivity = 0.002f;
    private boolean flying = false;

    public FlyCameraController(Camera camera, Application application) {
        super(camera);
        if (application == null) throw new IllegalArgumentException("Application cannot be null");
        this.application = application;
    }

    @Override
    public void updateCamera(Camera camera, SceneUpdateContext context) {
        handleCursorCapture(context);

        if (cursorCaptured) {
            move(camera, context);
            look(camera, context);
        }
    }

    public FlyCameraController setMoveSpeed(float moveSpeed) {
        if (!Float.isFinite(moveSpeed) || moveSpeed < 0.0f)
            throw new IllegalArgumentException("Move speed must be finite and non-negative");

        this.moveSpeed = moveSpeed;
        return this;
    }

    public FlyCameraController setSprintMultiplier(float sprintMultiplier) {
        if (!Float.isFinite(sprintMultiplier) || sprintMultiplier < 1.0f)
            throw new IllegalArgumentException("Sprint multiplier must be finite and at least 1");

        this.sprintMultiplier = sprintMultiplier;
        return this;
    }

    public FlyCameraController setMouseSensitivity(float mouseSensitivity) {
        if (!Float.isFinite(mouseSensitivity) || mouseSensitivity < 0.0f)
            throw new IllegalArgumentException("Mouse sensitivity must be finite and non-negative");

        this.mouseSensitivity = mouseSensitivity;
        return this;
    }

    public FlyCameraController setFlying(boolean flying) {
        this.flying = flying;
        return this;
    }

    private void handleCursorCapture(SceneUpdateContext context) {
        var input = context.getInput();
        var window = context.getWindow();

        // toggle capture
        if (input.isKeyPressed(Key.ESCAPE)) {
            if (cursorCaptured) {
                cursorCaptured = false;
                window.setCursorCaptured(false);
            } else {
                application.stop();
            }
        }

        if (input.isMousePressed(MouseButton.LMB)) {
            cursorCaptured = true;
            window.setCursorCaptured(true);
        }
    }

    private void move(Camera camera, SceneUpdateContext context) {
        var input = context.getInput();

        float speed = moveSpeed
                * ((input.isKeyDown(Key.LEFT_SHIFT)) ? sprintMultiplier : 1.0f)
                * (float) context.getDeltaTime();

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

    private void look(Camera camera, SceneUpdateContext context) {
        var input = context.getInput();

        var cameraTransform = camera.getTransform();

        var rotation = cameraTransform.getRotation();

        rotation.y -= (float) (input.getMouseDeltaX() * mouseSensitivity);
        rotation.x -= (float) (input.getMouseDeltaY() * mouseSensitivity);

        // clamp pitch to not flip upside down
        float maxPitch = (float) Math.toRadians(89.0f);
        rotation.x = Math.clamp(rotation.x, -maxPitch, maxPitch);

        cameraTransform.setRotation(rotation.x, rotation.y, rotation.z);
    }
}
