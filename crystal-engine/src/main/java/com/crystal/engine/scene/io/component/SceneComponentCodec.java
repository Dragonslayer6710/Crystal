package com.crystal.engine.scene.io.component;

import com.crystal.engine.scene.SceneComponent;

import static com.crystal.engine.scene.io.SceneDefinition.ComponentDefinition;

public interface SceneComponentCodec<T extends SceneComponent> {

    String type();

    Class<T> componentClass();

    T read(String objectName, ComponentDefinition definition);

    ComponentDefinition write(T component);
}
