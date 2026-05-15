package com.crystal.engine.render.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Transform {

    private Transform parent;

    private final Vector3f position = new Vector3f(0, 0, 0);
    private final Vector3f rotation = new Vector3f(0, 0, 0); // Euler for now
    private final Quaternionf rotationQuat = new Quaternionf();
    private final Vector3f scale    = new Vector3f(1, 1, 1);

    private final Matrix4f localMatrix = new Matrix4f();
    private final Matrix4f worldMatrix = new Matrix4f();

    public Transform() {

    }

    public Transform(float xPos, float yPos, float zPos) {
        this.position.set(xPos, yPos, zPos);
    }

    public Transform getParent() {
        return parent;
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public Vector3f getRotation() {
        return new Vector3f(rotation);
    }

    public Vector3f getScale() {
        return new Vector3f(scale);
    }

    public Transform setParent(Transform parent) {
        this.parent = parent;
        return this;
    }

    public Transform setPosition(float x, float y, float z) {
        position.set(x, y, z);
        return this;
    }

    public Transform setScale(float x, float y, float z) {
        scale.set(x, y, z);
        return this;
    }

    public Transform setScale(float value) {
        scale.set(value, value, value);
        return this;
    }

    public Transform setRotation(Quaternionf rotation) {
        if (rotation == null) {
            throw new IllegalArgumentException("Rotation cannot be null");
        }

        this.rotationQuat.set(rotation);
        this.rotation.set(rotation.getEulerAnglesXYZ(new Vector3f()));
        return this;
    }

    public Transform setRotation(float x, float y, float z) {
        rotation.set(x, y, z);
        rotationQuat.identity().rotateXYZ(x, y, z);
        return this;
    }

    public Transform setRotationDegrees(float x, float y, float z) {
        return setRotation(
                (float) Math.toRadians(x),
                (float) Math.toRadians(y),
                (float) Math.toRadians(z)
        );
    }

    public Transform translate(float x, float y, float z) {
        position.add(x, y, z);
        return this;
    }

    public Transform rotate(float x, float y, float z) {
        rotation.add(x, y, z);
        rotationQuat.rotateXYZ(x, y, z);
        return this;
    }

    public Transform rotateDegrees(float x, float y, float z) {
        return rotate(
                (float) Math.toRadians(x),
                (float) Math.toRadians(y),
                (float) Math.toRadians(z)
        );
    }

    public Transform scaleBy(float x, float y, float z) {
        scale.mul(x, y, z);
        return this;
    }

    public Matrix4f getLocalMatrix() {
        localMatrix.identity()
                .translate(position)
                .rotate(rotationQuat)
                .scale(scale);

        return localMatrix;
    }

    public Matrix4f getWorldMatrix() {
        if (parent == null)
            return worldMatrix.set(getLocalMatrix());

        return parent.getWorldMatrix().mul(getLocalMatrix(), worldMatrix);
    }

    public Vector3f getWorldPosition() {
        return getWorldMatrix().getTranslation(new Vector3f());
    }

    public Vector3f getWorldScale() {
        return getWorldMatrix().getScale(new Vector3f());
    }

}
