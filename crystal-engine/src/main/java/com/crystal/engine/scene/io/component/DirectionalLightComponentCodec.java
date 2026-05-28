package com.crystal.engine.scene.io.component;

import com.crystal.engine.scene.component.DirectionalLightComponent;

import static com.crystal.engine.scene.io.SceneDefinition.ComponentDefinition;
import static com.crystal.engine.scene.io.SceneDefinitionValues.requiredVec3;
import static com.crystal.engine.scene.io.SceneDefinitionValues.vec3;

final class DirectionalLightComponentCodec implements SceneComponentCodec<DirectionalLightComponent> {

    @Override
    public String type() {
        return "directionalLight";
    }

    @Override
    public Class<DirectionalLightComponent> componentClass() {
        return DirectionalLightComponent.class;
    }

    @Override
    public DirectionalLightComponent read(String objectName, ComponentDefinition definition) {
        DirectionalLightComponent light = new DirectionalLightComponent();

        if (definition.direction != null) {
            float[] direction = requiredVec3(
                definition.direction,
                objectName + ".directionalLight.direction"
            );
            light.setDirection(direction[0], direction[1], direction[2]);
        }


        if (definition.color != null) {
            float[] color = requiredVec3(
                definition.color,
                objectName + ".directionalLight.color"
            );
            light.setColor(color[0], color[1], color[2]);
        }

        if (definition.intensity != null)
            light.setIntensity(definition.intensity);

        if (definition.shadowStrength != null)
            light.setShadowStrength(definition.shadowStrength);

        if (definition.useTransformDirection != null)
            light.setUseTransformDirection(definition.useTransformDirection);

        return light;
    }

    @Override
    public ComponentDefinition write(DirectionalLightComponent component) {
        var light = component.getLight();

        ComponentDefinition definition = new ComponentDefinition();
        definition.type = type();
        definition.direction = vec3(light.getDirection());
        definition.color = vec3(light.getColor());
        definition.intensity = light.getIntensity();
        definition.shadowStrength = light.getShadowStrength();
        definition.useTransformDirection = component.usesTransformDirection();

        return definition;
    }
}
