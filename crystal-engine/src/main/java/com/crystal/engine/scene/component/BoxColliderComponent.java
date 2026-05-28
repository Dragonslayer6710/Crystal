package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.Transform;
import com.crystal.engine.scene.collision.BoxCollider;
import org.joml.Vector3f;

public class BoxColliderComponent extends SceneComponent {

    private final BoxCollider collider = new BoxCollider();

    public BoxColliderComponent() {}

    public BoxColliderComponent(float halfWidth, float halfHeight, float halfDepth) {
        setHalfExtents(halfWidth, halfHeight, halfDepth);
    }

    public BoxCollider getCollider() {
        return collider;
    }

    public Vector3f getHalfExtents() {
        return collider.getHalfExtents();
    }

    public BoxColliderComponent setHalfExtents(float halfWidth, float halfHeight, float halfDepth) {
        collider.setHalfExtents(halfWidth, halfHeight, halfDepth);
        return this;
    }

    public boolean contains(Vector3f point) {
        if (getOwner() == null)
            return false;

        return collider.contains(point, getOwner().getTransform());
    }

    public boolean intersects(BoxColliderComponent other) {
        if (other == null) throw new IllegalArgumentException("Other collider cannot be null");
        if (getOwner() == null || other.getOwner() == null)
            return false;

        return collider.intersects(
            other.collider,
            getOwner().getTransform(),
            other.getOwner().getTransform()
        );
    }

    public boolean intersects(BoxCollider other, Transform otherTransform) {
        if (other == null) throw new IllegalArgumentException("Other collider cannot be null");
        if (otherTransform == null) throw new IllegalArgumentException("Other transform cannot be null");
        if (getOwner() == null)
            return false;

        return collider.intersects(
            other,
            getOwner().getTransform(),
            otherTransform
        );
    }
}
