package com.crystal.engine.render.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transform {

    private final Vector3f position = new Vector3f(0, 0, 0);
    private final Vector3f rotation = new Vector3f(0, 0, 0); // Euler for now
    private final Vector3f scale    = new Vector3f(1, 1, 1);

    private final Matrix4f modelMatrix = new Matrix4f();

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    public Transform setPosition(float x, float y, float z) {
        position.set(x, y, z);
        return this;
    }

    public Transform setScale(float x, float y, float z) {
        scale.set(x, y, z);
        return this;
    }

    public Transform setRotation(float x, float y, float z) {
        rotation.set(x, y, z);
        return this;
    }

    public Matrix4f getModelMatrix() {
        modelMatrix.identity()
                .translate(position)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z)
                .scale(scale);

        return modelMatrix;
    }
}