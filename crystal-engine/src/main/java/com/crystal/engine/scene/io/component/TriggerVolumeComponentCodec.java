package com.crystal.engine.scene.io.component;

import com.crystal.engine.scene.component.TriggerVolumeComponent;

import static com.crystal.engine.scene.io.SceneDefinition.ComponentDefinition;
import static com.crystal.engine.scene.io.SceneDefinitionValues.requiredVec3;
import static com.crystal.engine.scene.io.SceneDefinitionValues.vec3;

public class TriggerVolumeComponentCodec implements SceneComponentCodec<TriggerVolumeComponent> {

    @Override
    public String type() {
        return "triggerVolume";
    }

    @Override
    public Class<TriggerVolumeComponent> componentClass() {
        return TriggerVolumeComponent.class;
    }

    @Override
    public TriggerVolumeComponent read(String objectName, ComponentDefinition definition) {
        float[] halfExtents = requiredVec3(
            definition.halfExtents,
            objectName + ".triggerVolume.halfExtents"
        );

        return new TriggerVolumeComponent(
            halfExtents[0],
            halfExtents[1],
            halfExtents[2]
        );
    }

    @Override
    public ComponentDefinition write(TriggerVolumeComponent component) {
        ComponentDefinition definition = new ComponentDefinition();
        definition.type = type();
        definition.halfExtents = vec3(component.getHalfExtents());

        return definition;
    }
}
