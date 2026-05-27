package com.crystal.engine.scene.io;

import com.crystal.engine.render.RenderLayers;
import com.crystal.engine.scene.Scene;
import com.crystal.engine.scene.component.*;
import com.crystal.engine.scene.source.SceneMaterialSource;
import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.scene.source.SceneObjectSource;
import com.crystal.engine.scene.animation.TransformKeyframe;
import com.crystal.engine.scene.source.SceneTransformSource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.crystal.engine.scene.io.SceneDefinition.*;

public final class SceneWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

    private SceneWriter() {
    }

    public static void write(Path scenePath, Scene scene) {
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");

        writeDefinition(scenePath, toDefinition(scene));
    }

    public static void writeDefinition(Path scenePath, SceneDefinition definition) {
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

        if (scene.getActiveDirectionalLight().isEmpty())
            definition.lighting = createLightingDefinition(scene);

        definition.materials = scene.getMaterialSources()
            .stream()
            .map(SceneWriter::createMaterialDefinition)
            .toList();

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

        if (scene.getEnvironmentSource() != null)
            definition.ibl = scene.getEnvironmentSource().getIbl();

        return definition;
    }

    private static LightingDefinition createLightingDefinition(Scene scene) {
        LightingDefinition definition = new LightingDefinition();

        var light = scene.getDirectionalLight();

        definition.directionalIntensity = light.getIntensity();
        definition.shadowStrength = light.getShadowStrength();

        return definition;
    }

    private static MaterialDefinition createMaterialDefinition(SceneMaterialSource source) {
        MaterialDefinition definition = new MaterialDefinition();

        definition.name = source.getName();
        definition.albedo = source.getAlbedo();
        definition.normal = source.getNormal();
        definition.roughness = source.getRoughness();
        definition.metallic = source.getMetallic();
        definition.normalStrength = source.getNormalStrength();

        return definition;
    }

    private static ObjectDefinition createObjectDefinition(SceneObject object) {
        ObjectDefinition definition = new ObjectDefinition();

        var layerMask = object.getLayerMask();

        definition.name = object.getName();

        applySource(object, definition);
        applyTransform(object, definition);

        if (!object.getTags().isEmpty())
            definition.tags = new ArrayList<>(object.getTags());

        if (layerMask != RenderLayers.WORLD)
            definition.layerMask = layerMask;

        if (!object.castsShadow())
            definition.castsShadow = false;

        applyTrigger(object, definition);
        applyComponents(object, definition);

        if (!isImportedModelRoot(object) && !object.getChildren().isEmpty()) {
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

    private static void applyTransform(SceneObject object, ObjectDefinition definition) {
        SceneTransformSource source = object.getTransformSource();

        if (source != null) {
            var position = source.getPosition();
            var rotation = source.getRotationDegrees();
            var scale = source.getScale();

            if (position != null)
                definition.position = vec3(position);

            if (rotation != null)
                definition.rotationDegrees = vec3(rotation);

            if (scale != null)
                definition.scale = vec3(scale);

            return;
        }

        var transform = object.getTransform();
        var position = transform.getPosition();
        var rotation = transform.getRotation();
        var scale = transform.getScale();

        if (!isZero(position))
            definition.position = vec3(position);

        if (!isZero(rotation))
            definition.rotationDegrees = vec3Degrees(rotation);

        if (!isOne(scale))
            definition.scale = vec3(scale);
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

        BobComponent bob = object.getComponent(BobComponent.class);

        if (bob != null) {
            ComponentDefinition component = new ComponentDefinition();
            component.type = "bob";
            component.amplitude = bob.getAmplitude();
            component.speed = bob.getSpeed();
            component.phase = bob.getPhase();
            components.add(component);
        }

        OrbitComponent orbit = object.getComponent(OrbitComponent.class);

        if (orbit != null) {
            ComponentDefinition component = new ComponentDefinition();
            component.type = "orbit";
            component.center = vec3(orbit.getCenter());
            component.radius = orbit.getRadius();
            component.speed = orbit.getSpeed();
            component.phase = orbit.getPhase();
            components.add(component);
        }

        DirectionalLightComponent directionalLight = object.getComponent(DirectionalLightComponent.class);

        if (directionalLight != null) {
            var light = directionalLight.getLight();

            ComponentDefinition component = new ComponentDefinition();
            component.type = "directionalLight";
            component.direction = vec3(light.getDirection());
            component.color = vec3(light.getColor());
            component.intensity = light.getIntensity();
            component.shadowStrength = light.getShadowStrength();
            component.useTransformDirection = directionalLight.usesTransformDirection();

            components.add(component);
        }

        PointLightComponent pointLight = object.getComponent(PointLightComponent.class);

        if (pointLight != null) {
            var light = pointLight.getLight();

            ComponentDefinition component = new ComponentDefinition();
            component.type = "pointLight";
            component.color = vec3(light.getColor());
            component.intensity = light.getIntensity();
            component.radius = light.getRadius();

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

    private static boolean isImportedModelRoot(SceneObject object) {
        SceneObjectSource source = object.getSource();

        return source != null && source.getType() == SceneObjectSource.Type.MODEL;
    }

    private static List<Float> vec3(Vector3fc value) {
        return List.of(value.x(), value.y(), value.z());
    }

    private static List<Float> vec3Degrees(Vector3fc radians) {
        return List.of(
            (float) Math.toDegrees(radians.x()),
            (float) Math.toDegrees(radians.y()),
            (float) Math.toDegrees(radians.z())
        );
    }

    private static boolean isZero(Vector3fc value) {
        return value.x() == 0.0f && value.y() == 0.0f && value.z() == 0.0f;
    }

    private static boolean isOne(Vector3fc value) {
        return value.x() == 1.0f && value.y() == 1.0f && value.z() == 1.0f;
    }
}
