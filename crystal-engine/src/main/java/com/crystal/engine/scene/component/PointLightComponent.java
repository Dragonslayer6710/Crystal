package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;
import com.crystal.engine.scene.light.PointLight;

public class PointLightComponent extends SceneComponent {

    private final PointLight light = new PointLight();

    public PointLight getLight() {
        return light;
    }

    public PointLightComponent setColor(float r, float g, float b) {
        light.setColor(r, g, b);
        return this;
    }

    public PointLightComponent setIntensity(float intensity) {
        light.setIntensity(intensity);
        return this;
    }

    public PointLightComponent setRadius(float radius) {
        light.setRadius(radius);
        return this;
    }

    @Override
    public void update(SceneUpdateContext context) {
        if (getOwner() == null)
            return;

        var position = getOwner().getTransform().getWorldPosition();
        light.setPosition(position.x, position.y, position.z);
    }
}
