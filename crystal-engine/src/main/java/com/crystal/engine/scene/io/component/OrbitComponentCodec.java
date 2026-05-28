package com.crystal.engine.scene.io.component;

import com.crystal.engine.scene.component.OrbitComponent;

import static com.crystal.engine.scene.io.SceneDefinition.ComponentDefinition;
import static com.crystal.engine.scene.io.SceneDefinitionValues.requiredVec3;
import static com.crystal.engine.scene.io.SceneDefinitionValues.vec3;

final class OrbitComponentCodec implements SceneComponentCodec<OrbitComponent> {

    @Override
    public String type() {
        return "orbit";
    }

    @Override
    public Class<OrbitComponent> componentClass() {
        return OrbitComponent.class;
    }

    @Override
    public OrbitComponent read(String objectName, ComponentDefinition definition) {
        OrbitComponent orbit = new OrbitComponent();

        if (definition.center != null) {
            float[] center = requiredVec3(definition.center, objectName + ".orbit.center");
            orbit.setCenter(center[0], center[1], center[2]);
        }

        if (definition.radius != null)
            orbit.setRadius(definition.radius);

        if (definition.speed != null)
            orbit.setSpeed(definition.speed);

        if (definition.phase != null)
            orbit.setPhase(definition.phase);

        return orbit;
    }

    @Override
    public ComponentDefinition write(OrbitComponent component) {
        ComponentDefinition definition = new ComponentDefinition();
        definition.type = type();
        definition.center = vec3(component.getCenter());
        definition.radius = component.getRadius();
        definition.speed = component.getSpeed();
        definition.phase = component.getPhase();

        return definition;
    }
}
