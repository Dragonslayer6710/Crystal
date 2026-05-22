package com.crystal.engine.render.scene.animation;

import org.joml.Vector3f;

public record TransformKeyframe(
    double time,
    Vector3f position,
    Vector3f rotationDegrees,
    Vector3f scale
) {
    public TransformKeyframe {
        if (!Double.isFinite(time) || time < 0.0)
            throw new IllegalArgumentException("Keyframe time must be finite and non-negative");
    }
}
