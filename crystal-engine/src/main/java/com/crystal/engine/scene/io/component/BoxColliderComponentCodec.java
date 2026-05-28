package com.crystal.engine.scene.io.component;

import com.crystal.engine.scene.component.BoxColliderComponent;

import static com.crystal.engine.scene.io.SceneDefinition.ComponentDefinition;
import static com.crystal.engine.scene.io.SceneDefinitionValues.requiredVec3;
import static com.crystal.engine.scene.io.SceneDefinitionValues.vec3;

public class BoxColliderComponentCodec implements SceneComponentCodec<BoxColliderComponent> {

    @Override
    public String type() {
        return "boxCollider";
    }

    @Override
    public Class<BoxColliderComponent> componentClass() {
        return BoxColliderComponent.class;
    }

    @Override
    public BoxColliderComponent read(String objectName, ComponentDefinition definition) {
        float[] halfExtents = requiredVec3(
            definition.halfExtents,
            objectName + ".boxCollider.halfExtents"
        );

        return new BoxColliderComponent(
            halfExtents[0],
            halfExtents[1],
            halfExtents[2]
        );
    }

    @Override
    public ComponentDefinition write(BoxColliderComponent component) {
        ComponentDefinition definition = new ComponentDefinition();
        definition.type = type();
        definition.halfExtents = vec3(component.getHalfExtents());

        return definition;
    }
}
