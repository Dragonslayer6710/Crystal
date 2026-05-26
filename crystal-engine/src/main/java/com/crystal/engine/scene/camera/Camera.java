package com.crystal.engine.scene.camera;

import com.crystal.engine.scene.Transform;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private final float fovy = 70f;
    private final float zNear = 0.1f;
    private final float zFar = 100f;

    private final Transform transform = new Transform();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();

    private final Frustum frustum = new Frustum();
    private final Matrix4f viewProjectionMatrix = new Matrix4f();

    public Camera(float xPos, float yPos, float zPos) {
        transform.setPosition(xPos, yPos, zPos);
    }

    public Transform getTransform() {
        return transform;
    }

    public Matrix4f getViewMatrix() {
        var position = transform.getPosition();
        var rotation = transform.getRotation();

        viewMatrix.identity()
                .rotateX(-rotation.x)
                .rotateY(-rotation.y)
                .rotateZ(-rotation.z)
                .translate(-position.x, -position.y, -position.z);

        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix(float aspectRatio) {
        return projectionMatrix.identity().perspective(
                (float) Math.toRadians(fovy),
                aspectRatio,
                zNear,
                zFar
        );
    }

    public Vector3f getForward() {
        var rotation = transform.getRotation();

        return new Vector3f(0, 0, -1)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z)
                .normalize();
    }

    public Vector3f getRight() {
        var rotation = transform.getRotation();

        return new Vector3f(1, 0, 0)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z)
                .normalize();
    }

    public Vector3f getForwardXZ() {
        float yaw = transform.getRotation().y;

        return new Vector3f(
                (float) -Math.sin(yaw),
                0.0f,
                (float) -Math.cos(yaw)
        ).normalize();
    }

    public Vector3f getRightXZ() {
        float yaw = transform.getRotation().y;

        return new Vector3f(
                (float) Math.cos(yaw),
                0.0f,
                (float) -Math.sin(yaw)
        ).normalize();
    }

    public void updateFrustum(float aspectRatio) {
        viewProjectionMatrix
                .set(getProjectionMatrix(aspectRatio))
                .mul(getViewMatrix());

        frustum.update(viewProjectionMatrix);
    }

    public boolean canSee(Vector3f center, float radius) {
       return frustum.containsSphere(center, radius);
    }
}
