package com.crystal.engine.scene.collision;

import com.crystal.engine.scene.Transform;
import org.joml.Vector3f;

public final class BoxCollider {

    private final BoxVolume volume = new BoxVolume();

    public BoxCollider() {}

    public BoxCollider(float halfWidth, float halfHeight, float halfDepth) {
        setHalfExtents(halfWidth, halfHeight, halfDepth);
    }

    public Vector3f getHalfExtents() {
        return volume.getHalfExtents();
    }

    public BoxCollider setHalfExtents(float halfWidth, float halfHeight, float halfDepth) {
        volume.setHalfExtents(halfWidth, halfHeight, halfDepth);
        return this;
    }

    public boolean contains(Vector3f point, Transform transform) {
        return volume.contains(point, transform);
    }

    public boolean intersects(BoxCollider other, Transform transform, Transform otherTransform) {
        if (other == null) throw new IllegalArgumentException("Other collider cannot be null");

        return volume.intersects(other.volume, transform, otherTransform);
    }
}
