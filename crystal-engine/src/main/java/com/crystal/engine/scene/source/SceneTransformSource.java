package com.crystal.engine.scene.source;

import org.joml.Vector3f;

public final class SceneTransformSource {

    private final Vector3f position;
    private final Vector3f rotationDegrees;
    private final Vector3f scale;

    public SceneTransformSource(Vector3f position, Vector3f rotationDegrees, Vector3f scale) {
        this.position = position == null ? null : new Vector3f(position);
        this.rotationDegrees = rotationDegrees == null ? null : new Vector3f(rotationDegrees);
        this.scale = scale == null ? null : new Vector3f(scale);
    }

    public Vector3f getPosition() {
        return position == null ? null : new Vector3f(position);
    }

    public Vector3f getRotationDegrees() {
        return rotationDegrees == null ? null : new Vector3f(rotationDegrees);
    }

    public Vector3f getScale() {
        return scale == null ? null : new Vector3f(scale);
    }
}
