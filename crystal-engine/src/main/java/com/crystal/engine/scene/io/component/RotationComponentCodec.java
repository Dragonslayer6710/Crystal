package com.crystal.engine.scene.io.component;

import com.crystal.engine.scene.component.RotationComponent;

import java.util.List;

import static com.crystal.engine.scene.io.SceneDefinitionValues.requiredVec3;

import static com.crystal.engine.scene.io.SceneDefinition.ComponentDefinition;

final class RotationComponentCodec implements SceneComponentCodec<RotationComponent> {

    @Override
    public String type() {
        return "rotation";
    }

    @Override
    public Class<RotationComponent> componentClass() {
        return RotationComponent.class;
    }

    @Override
    public RotationComponent read(String objectName, ComponentDefinition definition) {
        float[] speed = requiredVec3(definition.speedRadiansPerSecond, objectName + ".rotation.speedRadiansPerSecond");
        return new RotationComponent(speed[0], speed[1], speed[2]);
    }

    @Override
    public ComponentDefinition write(RotationComponent component) {
        ComponentDefinition definition = new ComponentDefinition();
        definition.type = type();
        definition.speedRadiansPerSecond = List.of(
            component.getXRadiansPerSecond(),
            component.getYRadiansPerSecond(),
            component.getZRadiansPerSecond()
        );

        return definition;
    }
}
