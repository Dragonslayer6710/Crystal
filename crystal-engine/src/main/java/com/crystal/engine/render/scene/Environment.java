package com.crystal.engine.render.scene;

import org.joml.Vector3f;

public class Environment {

    private final Vector3f ambientColor = new Vector3f(0.03f);
    private float ambientIntensity = 1.0f;

    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    public float getAmbientIntensity() {
        return ambientIntensity;
    }

    public Environment setAmbientColor(float r, float g, float b) {
        ambientColor.set(r, g, b);
        return this;
    }

    public Environment setAmbientIntensity(float intensity) {
        this.ambientIntensity = intensity;
        return this;
    }
}
