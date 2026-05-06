package com.crystal.engine.render.scene;

import org.joml.Vector3f;

public class DirectionalLight {

    private final Vector3f direction = new Vector3f(-1.0f, -1.0f, -0.5f);
    private final Vector3f color = new Vector3f(1.0f);

    private float intensity = 1.0f;

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getIntensity() {
        return intensity;
    }

    public DirectionalLight setDirection(float x, float y, float z) {
        direction.set(x, y, z);
        return this;
    }

    public DirectionalLight setColor(float x, float y, float z) {
        color.set(x, y, z);
        return this;
    }

    public DirectionalLight setIntensity(float intensity) {
        this.intensity = intensity;
        return this;
    }
}
