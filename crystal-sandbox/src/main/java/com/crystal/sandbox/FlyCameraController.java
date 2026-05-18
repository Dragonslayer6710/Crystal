package com.crystal.sandbox;

import com.crystal.engine.core.Application;
import com.crystal.engine.core.EngineContext;
import com.crystal.engine.input.Key;
import com.crystal.engine.input.MouseButton;
import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.CameraControllerComponent;
import com.crystal.engine.render.scene.SceneUpdateContext;
import org.joml.Vector3f;

public class FlyCameraController extends CameraControllerComponent {

    private final Application application;

    private boolean cursorCaptured = false;
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

        float speed = ((input.isKeyDown(Key.LEFT_SHIFT)) ? 2f : 1f) * (float) context.getDeltaTime();

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

        float mouseSensitivity = 0.002f;

        var rotation = cameraTransform.getRotation();

        rotation.y -= (float) (input.getMouseDeltaX() * mouseSensitivity);
        rotation.x -= (float) (input.getMouseDeltaY() * mouseSensitivity);

        // clamp pitch to not flip upside down
        float maxPitch = (float) Math.toRadians(89.0f);
        rotation.x = Math.clamp(rotation.x, -maxPitch, maxPitch);

        cameraTransform.setRotation(rotation.x, rotation.y, rotation.z);
    }
}
