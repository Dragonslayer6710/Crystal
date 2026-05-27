package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;
import com.crystal.engine.scene.light.DirectionalLight;
import org.joml.Vector3f;

public class DirectionalLightComponent extends SceneComponent {

    private final DirectionalLight light = new DirectionalLight();
    private boolean useTransformDirection;

    public DirectionalLight getLight() {
        return light;
    }

    public boolean usesTransformDirection() {
        return useTransformDirection;
    }

    public DirectionalLightComponent setDirection(float x, float y, float z) {
        light.setDirection(x, y, z);
        return this;
    }

    public DirectionalLightComponent setColor(float r, float g, float b) {
        light.setColor(r, g, b);
        return this;
    }

    public DirectionalLightComponent setIntensity(float intensity) {
        light.setIntensity(intensity);
        return this;
    }

    public DirectionalLightComponent setShadowStrength(float shadowStrength) {
        light.setShadowStrength(shadowStrength);
        return this;
    }

    public DirectionalLightComponent setUseTransformDirection(boolean useTransformDirection) {
        this.useTransformDirection = useTransformDirection;
        return this;
    }

    @Override
    public void update(SceneUpdateContext context) {
        if (!useTransformDirection || getOwner() == null)
            return;

        Vector3f direction = new Vector3f(0.0f, 0.0f, -1.0f)
            .rotate(getOwner().getTransform().getWorldRotationQuat())
            .normalize();

        light.setDirection(direction.x, direction.y, direction.z);
    }
}
