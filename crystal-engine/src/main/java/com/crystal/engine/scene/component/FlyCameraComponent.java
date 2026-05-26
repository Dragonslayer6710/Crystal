package com.crystal.engine.scene.component;

import com.crystal.engine.input.Key;
import com.crystal.engine.input.action.InputAction;
import com.crystal.engine.input.action.InputMap;
import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;
import org.joml.Vector3f;

public class FlyCameraComponent extends SceneComponent {

    private float moveSpeed = 1.0f;
    private float sprintMultiplier = 2.0f;
    private float mouseSensitivity = 0.002f;
    private boolean flying = false;

    private final InputMap inputMap = new InputMap();

    private static final InputAction MOVE_FORWARD = new InputAction("camera_move_forward");
    private static final InputAction MOVE_BACKWARD = new InputAction("camera_move_backward");
    private static final InputAction MOVE_LEFT = new InputAction("camera_move_left");
    private static final InputAction MOVE_RIGHT = new InputAction("camera_move_right");
    private static final InputAction MOVE_UP = new InputAction("camera_move_up");
    private static final InputAction MOVE_DOWN = new InputAction("camera_move_down");
    private static final InputAction SPRINT = new InputAction("camera_sprint");

    public FlyCameraComponent() {
        inputMap
            .bind(MOVE_FORWARD, Key.W)
            .bind(MOVE_BACKWARD, Key.S)
            .bind(MOVE_LEFT, Key.A)
            .bind(MOVE_RIGHT, Key.D)
            .bind(MOVE_UP, Key.SPACE)
            .bind(MOVE_DOWN, Key.LEFT_CTRL)
            .bind(SPRINT, Key.LEFT_SHIFT);
    }

    @Override
    public void update(SceneUpdateContext context) {
        if (!context.getWindow().isCursorCaptured())
            return;

        look(context);
        move(context);
    }

    public FlyCameraComponent setMoveSpeed(float moveSpeed) {
        if (!Float.isFinite(moveSpeed) || moveSpeed < 0.0f)
            throw new IllegalArgumentException("Move speed must be finite and non-negative");

        this.moveSpeed = moveSpeed;
        return this;
    }

    public FlyCameraComponent setSprintMultiplier(float sprintMultiplier) {
        if (!Float.isFinite(sprintMultiplier) || sprintMultiplier < 1.0f)
            throw new IllegalArgumentException("Sprint multiplier must be finite and at least 1");

        this.sprintMultiplier = sprintMultiplier;
        return this;
    }

    public FlyCameraComponent setMouseSensitivity(float mouseSensitivity) {
        if (!Float.isFinite(mouseSensitivity) || mouseSensitivity < 0.0f)
            throw new IllegalArgumentException("Mouse sensitivity must be finite and non-negative");

        this.mouseSensitivity = mouseSensitivity;
        return this;
    }

    public FlyCameraComponent setFlying(boolean flying) {
        this.flying = flying;
        return this;
    }

    private void look(SceneUpdateContext context) {
        var input = context.getInput();

        var ownerTransform = getOwner().getTransform();

        var rotation = ownerTransform.getRotation();

        rotation.y -= (float) (input.getMouseDeltaX() * mouseSensitivity);
        rotation.x -= (float) (input.getMouseDeltaY() * mouseSensitivity);

        // clamp pitch to not flip upside down
        float maxPitch = (float) Math.toRadians(89.0f);
        rotation.x = Math.clamp(rotation.x, -maxPitch, maxPitch);

        ownerTransform.setRotation(rotation.x, rotation.y, rotation.z);
    }

    private void move(SceneUpdateContext context) {
        var input = context.getInput();

        float speed = moveSpeed
            * ((inputMap.isDown(input, SPRINT)) ? sprintMultiplier : 1.0f)
            * (float) context.getDeltaTime();


        var ownerTransform = getOwner().getTransform();

        var forward = (flying) ? ownerTransform.getForward() : ownerTransform.getForwardXZ();
        var right = (flying) ? ownerTransform.getRight() : ownerTransform.getRightXZ();

        Vector3f movement = new Vector3f();

        if (inputMap.isDown(input, MOVE_FORWARD) && !inputMap.isDown(input, MOVE_BACKWARD)) {
            movement.add(forward);
        } else if (inputMap.isDown(input, MOVE_BACKWARD)) {
            movement.sub(forward);
        }

        if (inputMap.isDown(input, MOVE_RIGHT) && !inputMap.isDown(input, MOVE_LEFT)) {
            movement.add(right);
        } else if (inputMap.isDown(input, MOVE_LEFT)) {
            movement.sub(right);
        }

        if (inputMap.isDown(input, MOVE_UP) && !inputMap.isDown(input, MOVE_DOWN)) {
            movement.y += 1.0f;
        } else if (inputMap.isDown(input, MOVE_DOWN)) {
            movement.y -= 1.0f;
        }

        if (movement.lengthSquared() > 0.0f) {
            movement.normalize();
            movement.mul(speed);
            getOwner().getTransform().translate(movement.x, movement.y, movement.z);
        }
    }
}
