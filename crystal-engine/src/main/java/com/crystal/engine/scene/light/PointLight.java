package com.crystal.engine.scene.light;

import org.joml.Vector3f;

public class PointLight {

    private final Vector3f position = new Vector3f();
    private final Vector3f color = new Vector3f(1.0f);

    private float intensity = 1.0f;
    private float radius = 10.0f;

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getIntensity() {
        return intensity;
    }

    public float getRadius() {
        return radius;
    }

    public PointLight setPosition(float x, float y, float z) {
        position.set(x, y, z);
        return this;
    }

    public PointLight setColor(float r, float g, float b) {
        color.set(r, g, b);
        return this;
    }

    public PointLight setIntensity(float intensity) {
        this.intensity = intensity;
        return this;
    }

    public PointLight setRadius(float radius) {
        this.radius = radius;
        return this;
    }
}
