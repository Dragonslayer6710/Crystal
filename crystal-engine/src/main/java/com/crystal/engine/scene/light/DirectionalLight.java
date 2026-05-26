package com.crystal.engine.scene.light;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DirectionalLight {

    private final Vector3f direction = new Vector3f(-1.0f, -1.0f, -0.5f);
    private final Vector3f color = new Vector3f(1.0f);

    private float intensity = 1.0f;

    private float shadowDistance = 20.0f;
    private float shadowNear = 1.0f;
    private float shadowFar = 50.0f;
    private float shadowStrength = 0.6f;

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getIntensity() {
        return intensity;
    }

    public Matrix4f getLightSpaceMatrix() {
        Vector3f direction = new Vector3f(this.direction).normalize();

        Matrix4f lightProjection = new Matrix4f()
                .ortho(-shadowDistance, shadowDistance,
                        -shadowDistance, shadowDistance,
                        shadowNear, shadowFar);

        Matrix4f lightView = new Matrix4f()
                .lookAt(
                        new Vector3f(direction).mul(-20.0f),
                        new Vector3f(0, 0, 0),
                        new Vector3f(0, 1, 0)
                );

        return lightProjection.mul(lightView);
    }

    public float getShadowStrength() {
        return shadowStrength;
    }

    public DirectionalLight setDirection(float x, float y, float z) {
        direction.set(x, y, z).normalize();
        return this;
    }

    public DirectionalLight setColor(float r, float g, float b) {
        color.set(r, g, b);
        return this;
    }

    public DirectionalLight setIntensity(float intensity) {
        this.intensity = intensity;
        return this;
    }

    public DirectionalLight setShadowStrength(float shadowStrength) {
        this.shadowStrength = shadowStrength;
        return this;
    }
}
