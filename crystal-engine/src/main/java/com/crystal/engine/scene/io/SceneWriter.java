package com.crystal.engine.scene.io;

import com.crystal.engine.scene.Scene;
import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.scene.SceneObjectSource;
import com.crystal.engine.scene.animation.KeyframeAnimationComponent;
import com.crystal.engine.scene.animation.TransformKeyframe;
import com.crystal.engine.scene.component.RotationComponent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.crystal.engine.scene.io.SceneDefinition.*;

public final class SceneWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private SceneWriter() {}

    public static void write(Path scenePath, Scene scene) {
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");

        write(scenePath, toDefinition(scene));
    }

    private static void write(Path scenePath, SceneDefinition definition) {
        if (scenePath == null) throw new IllegalArgumentException("Scene path cannot be null");
        if (definition == null) throw new IllegalArgumentException("Scene definition cannot be null");

        try {
            OBJECT_MAPPER.writeValue(scenePath.toFile(), definition);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to write scene: " + scenePath, e);
        }
    }

    private static SceneDefinition toDefinition(Scene scene) {
        SceneDefinition definition = new SceneDefinition();
        definition.name = "Exported Scene";
        definition.version = 1;

        definition.camera = createCameraDefinition(scene);
        definition.environment = createEnvironmentDefinition(scene);
        definition.lighting = createLightingDefinition(scene);
        definition.objects = scene.getRootObjects()
            .stream()
            .map(SceneWriter::createObjectDefinition)
            .toList();

        return definition;
    }

    private static CameraDefinition createCameraDefinition(Scene scene) {
        CameraDefinition definition = new CameraDefinition();

        var position = scene.getCamera().getTransform().getPosition();
        definition.position = List.of(position.x, position.y, position.z);

        return definition;
    }

    private static EnvironmentDefinition createEnvironmentDefinition(Scene scene) {
        EnvironmentDefinition definition = new EnvironmentDefinition();

        var environment = scene.getEnvironment();
        var ambientColor = environment.getAmbientColor();

        definition.ambientColor = List.of(
            ambientColor.x,
            ambientColor.y,
            ambientColor.z
        );
        definition.ambientIntensity = environment.getAmbientIntensity();
        definition.iblDiffuseIntensity = environment.getIblDiffuseIntensity();
        definition.iblSpecularIntensity = environment.getIblSpecularIntensity();

        return definition;
    }

    private static LightingDefinition createLightingDefinition(Scene scene) {
        LightingDefinition definition = new LightingDefinition();

        var light = scene.getDirectionalLight();

        definition.directionalIntensity = light.getIntensity();
        definition.shadowStrength = light.getShadowStrength();

        return definition;
    }

    private static ObjectDefinition createObjectDefinition(SceneObject object) {
        ObjectDefinition definition = new ObjectDefinition();

        var transform = object.getTransform();

        definition.name = object.getName();
        applySource(object, definition);
        definition.position = vec3(transform.getPosition());
        definition.rotationDegrees = vec3Degrees(transform.getRotation());
        definition.scale = vec3(transform.getScale());

        if (!object.getTags().isEmpty())
            definition.tags = new ArrayList<>(object.getTags());

        if (object.getLayerMask() != 0)
            definition.layerMask = object.getLayerMask();

        definition.castsShadow = object.castsShadow();

        applyTrigger(object, definition);
        applyComponents(object, definition);

        if (!object.getChildren().isEmpty()) {
            definition.children = object.getChildren()
                .stream()
                .map(SceneWriter::createObjectDefinition)
                .toList();
        }

        return definition;
    }

    private static void applySource(SceneObject object, ObjectDefinition definition) {
        SceneObjectSource source = object.getSource();

        if (source == null) {
            definition.type = "empty";
            return;
        }

        switch (source.getType()) {
            case EMPTY -> definition.type = "empty";
            case PRIMITIVE -> {
                definition.type = "primitive";
                definition.primitive = source.getPrimitive();
                definition.material = source.getMaterial();
            }
            case MODEL -> {
                definition.type = "model";
                definition.path = source.getPath();
            }
        }
    }

    private static void applyTrigger(SceneObject object, ObjectDefinition definition) {
        if (!object.hasTriggerVolume())
            return;

        TriggerDefinition trigger = new TriggerDefinition();
        trigger.halfExtents = vec3(object.getTriggerVolume().getHalfExtents());

        definition.trigger = trigger;
    }

    private static void applyComponents(SceneObject object, ObjectDefinition definition) {
        List<ComponentDefinition> components = new ArrayList<>();

        RotationComponent rotation = object.getComponent(RotationComponent.class);

        if (rotation != null) {
            ComponentDefinition component = new ComponentDefinition();
            component.type = "rotation";
            component.speedRadiansPerSecond = List.of(
                rotation.getXRadiansPerSecond(),
                rotation.getYRadiansPerSecond(),
                rotation.getZRadiansPerSecond()
            );

            components.add(component);
        }

        KeyframeAnimationComponent animation = object.getComponent(KeyframeAnimationComponent.class);

        if (animation != null) {
            ComponentDefinition component = new ComponentDefinition();
            component.type = "keyframeAnimation";
            component.loop = animation.isLoop();
            component.keyframes = animation.getKeyframes()
                .stream()
                .map(SceneWriter::createKeyframeDefinition)
                .toList();

            components.add(component);
        }

        if (!components.isEmpty())
            definition.components = components;
    }

    private static KeyframeDefinition createKeyframeDefinition(TransformKeyframe keyframe) {
        KeyframeDefinition definition = new KeyframeDefinition();

        definition.time = keyframe.time();

        if (keyframe.position() != null)
            definition.position = vec3(keyframe.position());

        if (keyframe.rotationDegrees() != null)
            definition.rotationDegrees = vec3(keyframe.rotationDegrees());

        if (keyframe.scale() != null)
            definition.scale = vec3(keyframe.scale());

        return definition;
    }

    private static List<Float> vec3(Vector3f value) {
        return List.of(value.x, value.y, value.z);
    }

    private static List<Float> vec3Degrees(Vector3f radians) {
        return List.of(
            (float) Math.toDegrees(radians.x),
            (float) Math.toDegrees(radians.y),
            (float) Math.toDegrees(radians.z)
        );
    }
}
