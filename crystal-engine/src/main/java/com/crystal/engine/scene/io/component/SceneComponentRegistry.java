package com.crystal.engine.scene.io.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.crystal.engine.scene.io.SceneDefinition.ComponentDefinition;

public final class SceneComponentRegistry {

    private final Map<String, SceneComponentCodec<? extends SceneComponent>> codecsByType = new HashMap<>();
    private final Map<Class<? extends SceneComponent>, SceneComponentCodec<? extends SceneComponent>> codecsByClass = new HashMap<>();

    public static SceneComponentRegistry createDefault() {
        return new SceneComponentRegistry()
            .register(new RotationComponentCodec())
            .register(new BobComponentCodec())
            .register(new OrbitComponentCodec())
            .register(new PointLightComponentCodec())
            .register(new DirectionalLightComponentCodec())
            .register(new KeyframeAnimationComponentCodec());
    }

    public SceneComponentRegistry register(SceneComponentCodec<? extends SceneComponent> codec) {
        if (codec == null) throw new IllegalArgumentException("Component codec cannot be null");

        if (codecsByType.containsKey(codec.type()))
            throw new IllegalArgumentException("Duplicate component codec type: " + codec.type());

        if (codecsByClass.containsKey(codec.componentClass()))
            throw new IllegalArgumentException("Duplicate component codec class: " + codec.componentClass().getName());

        codecsByType.put(codec.type(), codec);
        codecsByClass.put(codec.componentClass(), codec);

        return this;
    }

    public SceneComponent read(String objectName, ComponentDefinition definition) {
        if (definition == null) throw new IllegalArgumentException("Component definition cannot be null");
        if (definition.type == null || definition.type.isBlank())
            throw new IllegalArgumentException(objectName + ".component.type cannot be null or blank");

        SceneComponentCodec<? extends SceneComponent> codec = codecsByType.get(definition.type);

        if (codec == null)
            throw new IllegalArgumentException("Unsupported component type: " + definition.type);

        return codec.read(objectName, definition);
    }

    public List<ComponentDefinition> writeComponents(SceneObject object) {
        if (object == null) throw new IllegalArgumentException("SceneObject cannot be null");

        List<ComponentDefinition> definitions = new ArrayList<>();

        for (SceneComponent component : object.getComponents()) {
            ComponentDefinition definition = write(component);

            if (definition != null)
                definitions.add(definition);
        }

        return definitions;
    }

    private <T extends SceneComponent> ComponentDefinition write(SceneComponent component) {
        @SuppressWarnings("unchecked")
        SceneComponentCodec<T> codec = (SceneComponentCodec<T>) codecsByClass.get(component.getClass());

        if (codec == null)
            return null;

        return codec.write((T) component);
    }
}
