package com.crystal.engine.scene.io.component;

import com.crystal.engine.scene.component.PointLightComponent;

import static com.crystal.engine.scene.io.SceneDefinition.ComponentDefinition;
import static com.crystal.engine.scene.io.SceneDefinitionValues.requiredVec3;
import static com.crystal.engine.scene.io.SceneDefinitionValues.vec3;

final class PointLightComponentCodec implements SceneComponentCodec<PointLightComponent> {

    @Override
    public String type() {
        return "pointLight";
    }

    @Override
    public Class<PointLightComponent> componentClass() {
        return PointLightComponent.class;
    }

    @Override
    public PointLightComponent read(String objectName, ComponentDefinition definition) {
        PointLightComponent light = new PointLightComponent();

        if (definition.color != null) {
            float[] color = requiredVec3(
                definition.color,
                objectName + ".pointLight.color"
            );
            light.setColor(color[0], color[1], color[2]);
        }

        if (definition.intensity != null)
            light.setIntensity(definition.intensity);

        if (definition.radius != null)
            light.setRadius(definition.radius);

        return light;
    }

    @Override
    public ComponentDefinition write(PointLightComponent component) {
        var light = component.getLight();

        ComponentDefinition definition = new ComponentDefinition();
        definition.type = type();
        definition.color = vec3(light.getColor());
        definition.intensity = light.getIntensity();
        definition.radius = light.getRadius();

        return definition;
    }
}
