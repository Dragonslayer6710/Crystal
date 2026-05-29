package com.crystal.engine.scene.collision;

import com.crystal.engine.scene.Scene;
import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.scene.Transform;
import org.joml.Vector3f;

import java.util.List;

public final class CollisionMovement {

    private CollisionMovement() {}

    public static Vector3f move(
        Scene scene,
        SceneObject owner,
        BoxCollider collider,
        Transform transform,
        Vector3f desiredMovement
    ) {
        return move(scene, owner, collider, transform, new Vector3f(), desiredMovement);
    }

    public static Vector3f move(
        Scene scene,
        SceneObject owner,
        BoxCollider collider,
        Transform transform,
        Vector3f colliderCenterOffset,
        Vector3f desiredMovement
    ) {
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");
        if (owner == null) throw new IllegalArgumentException("Owner cannot be null");
        if (collider == null) throw new IllegalArgumentException("Collider cannot be null");
        if (transform == null) throw new IllegalArgumentException("Transform cannot be null");
        if (colliderCenterOffset == null) throw new IllegalArgumentException("Collider center offset cannot be null");
        if (desiredMovement == null) throw new IllegalArgumentException("Desired movement cannot be null");

        Transform colliderTransform = createColliderTransform(transform, colliderCenterOffset);
        Vector3f appliedMovement = new Vector3f();

        moveAxis(scene, owner, collider, transform, colliderTransform, desiredMovement.x, 0.0f, 0.0f, appliedMovement);
        moveAxis(scene, owner, collider, transform, colliderTransform, 0.0f, desiredMovement.y, 0.0f, appliedMovement);
        moveAxis(scene, owner, collider, transform, colliderTransform, 0.0f, 0.0f, desiredMovement.z, appliedMovement);

        return appliedMovement;
    }

    private static void moveAxis(
        Scene scene,
        SceneObject owner,
        BoxCollider collider,
        Transform ownerTransform,
        Transform colliderTransform,
        float x,
        float y,
        float z,
        Vector3f appliedMovement
    ) {
        if (x == 0.0f && y == 0.0f && z == 0.0f)
            return;

        ownerTransform.translate(x, y, z);
        colliderTransform.translate(x, y, z);

        if (intersectsBlockingCollider(scene, owner, collider, colliderTransform)) {
            ownerTransform.translate(-x, -y, -z);
            colliderTransform.translate(-x, -y, -z);
            return;
        }

        appliedMovement.add(x, y, z);
    }

    private static Transform createColliderTransform(Transform ownerTransform, Vector3f colliderCenterOffset) {
        var ownerPosition = ownerTransform.getWorldPosition();

        return new Transform().setPosition(
            ownerPosition.x + colliderCenterOffset.x,
            ownerPosition.y + colliderCenterOffset.y,
            ownerPosition.z + colliderCenterOffset.z
        );
    }

    private static boolean intersectsBlockingCollider(Scene scene, SceneObject owner,
                                                      BoxCollider collider, Transform transform) {
        List<SceneObject> collisions = scene.findCollidersIntersecting(collider, transform);

        for (SceneObject collision : collisions) {
            if (collision != owner)
                return true;
        }

        return false;
    }
}
