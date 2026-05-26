package com.crystal.engine.scene.collision;

import com.crystal.engine.scene.Transform;
import org.joml.Vector3f;

public final class TriggerVolume {

    private final Vector3f halfExtents = new Vector3f(0.5f);

    public TriggerVolume() {}

    public TriggerVolume(float halfWidth, float halfHeight, float halfDepth) {
        setHalfExtents(halfWidth, halfHeight, halfDepth);
    }

    public Vector3f getHalfExtents() {
        return new Vector3f(halfExtents);
    }

    public TriggerVolume setHalfExtents(float halfWidth, float halfHeight, float halfDepth) {
        if (!Float.isFinite(halfWidth) || halfWidth <= 0.0f)
            throw new IllegalArgumentException("Half width must be finite and greater than 0");

        if (!Float.isFinite(halfHeight) || halfHeight <= 0.0f)
            throw new IllegalArgumentException("Half height must be finite and greater than 0");

        if (!Float.isFinite(halfDepth) || halfDepth <= 0.0f)
            throw new IllegalArgumentException("Half depth must be finite and greater than 0");

        halfExtents.set(halfWidth, halfHeight, halfDepth);
        return this;
    }

    public boolean contains(Vector3f point, Transform transform) {
        if (point == null) throw new IllegalArgumentException("Point cannot be null");
        if (transform == null) throw new IllegalArgumentException("Transform cannot be null");

        Vector3f center = transform.getWorldPosition();

        return Math.abs(point.x - center.x) <= halfExtents.x
                && Math.abs(point.y - center.y) <= halfExtents.y
                && Math.abs(point.z - center.z) <= halfExtents.z;
    }
}
