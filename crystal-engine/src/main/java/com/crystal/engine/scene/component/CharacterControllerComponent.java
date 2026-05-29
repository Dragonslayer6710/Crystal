package com.crystal.engine.scene.component;

import com.crystal.engine.input.Key;
import com.crystal.engine.input.action.InputAction;
import com.crystal.engine.input.action.InputMap;
import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;

import com.crystal.engine.scene.collision.BoxCollider;
import com.crystal.engine.scene.collision.CollisionMovement;
import org.joml.Vector3f;

public class CharacterControllerComponent extends SceneComponent {

    private final BoxCollider collider = new BoxCollider(0.25f, 0.75f, 0.25f);

    private final Vector3f colliderCenterOffset = new Vector3f(0.0f, -0.75f, 0.0f);

    private final InputMap inputMap = new InputMap();

    private float moveSpeed = 2.0f;
    private float sprintMultiplier = 1.75f;

    private static final InputAction MOVE_FORWARD = new InputAction("character_move_forward");
    private static final InputAction MOVE_BACKWARD = new InputAction("character_move_backward");
    private static final InputAction MOVE_LEFT = new InputAction("character_move_left");
    private static final InputAction MOVE_RIGHT = new InputAction("character_move_right");
    private static final InputAction SPRINT = new InputAction("character_sprint");

    public CharacterControllerComponent() {
        inputMap
            .bind(MOVE_FORWARD, Key.W)
            .bind(MOVE_BACKWARD, Key.S)
            .bind(MOVE_LEFT, Key.A)
            .bind(MOVE_RIGHT, Key.D)
            .bind(SPRINT, Key.LEFT_SHIFT);
    }

    public BoxCollider getCollider() {
        return collider;
    }

    public Vector3f getHalfExtents() {
        return collider.getHalfExtents();
    }

    public Vector3f getColliderCenterOffset() {
        return new Vector3f(colliderCenterOffset);
    }

    public CharacterControllerComponent setHalfExtents(float halfWidth, float halfHeight, float halfDepth) {
        collider.setHalfExtents(halfWidth, halfHeight, halfDepth);
        return this;
    }

    public CharacterControllerComponent setColliderCenterOffset(float x, float y, float z) {
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z))
            throw new IllegalArgumentException("Collider center offset values must be finite");

        colliderCenterOffset.set(x, y, z);
        return this;
    }

    public CharacterControllerComponent setMoveSpeed(float moveSpeed) {
        if (!Float.isFinite(moveSpeed) || moveSpeed < 0.0f)
            throw new IllegalArgumentException("Move speed must be finite and non-negative");

        this.moveSpeed = moveSpeed;
        return this;
    }

    public CharacterControllerComponent setSprintMultiplier(float sprintMultiplier) {
        if (!Float.isFinite(sprintMultiplier) || sprintMultiplier < 1.0f)
            throw new IllegalArgumentException("Sprint multiplier must be finite and at least 1");

        this.sprintMultiplier = sprintMultiplier;
        return this;
    }

    @Override
    public void update(SceneUpdateContext context) {
        if (!context.getWindow().isCursorCaptured())
            return;

        Vector3f desiredMovement = buildDesiredMovement(context);

        if (desiredMovement.lengthSquared() == 0.0f)
            return;

        CollisionMovement.move(
            context.getScene(),
            getOwner(),
            collider,
            getOwner().getTransform(),
            colliderCenterOffset,
            desiredMovement
        );
    }

    private Vector3f buildDesiredMovement(SceneUpdateContext context) {
        var input = context.getInput();
        var transform = getOwner().getTransform();

        Vector3f forward = transform.getForwardXZ();
        Vector3f right = transform.getRightXZ();
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

        if (movement.lengthSquared() == 0.0f)
            return movement;

        float speed = moveSpeed
            * (inputMap.isDown(input, SPRINT) ? sprintMultiplier : 1.0f)
            * (float) context.getDeltaTime();

        return movement.normalize().mul(speed);
    }

}
