package com.crystal.engine.render.mesh;

import org.joml.Vector3f;

public record MeshBounds(
        Vector3f center,
        float radius
) {
    public MeshBounds {
        if (center == null) throw new IllegalArgumentException("Center cannot be null");
        if (radius < 0.0f) throw new IllegalArgumentException("Radius cannot be negative");
    }
}
