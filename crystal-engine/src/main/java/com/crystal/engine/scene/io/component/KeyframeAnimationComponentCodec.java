package com.crystal.engine.scene.io.component;


import com.crystal.engine.scene.animation.TransformKeyframe;
import com.crystal.engine.scene.component.KeyframeAnimationComponent;

import java.util.List;

import static com.crystal.engine.scene.io.SceneDefinition.ComponentDefinition;
import static com.crystal.engine.scene.io.SceneDefinition.KeyframeDefinition;
import static com.crystal.engine.scene.io.SceneDefinitionValues.optionalVec3;
import static com.crystal.engine.scene.io.SceneDefinitionValues.vec3;

final class KeyframeAnimationComponentCodec implements SceneComponentCodec<KeyframeAnimationComponent> {

    @Override
    public String type() {
        return "keyframeAnimation";
    }

    @Override
    public Class<KeyframeAnimationComponent> componentClass() {
        return KeyframeAnimationComponent.class;
    }

    @Override
    public KeyframeAnimationComponent read(String objectName, ComponentDefinition definition) {
        if (definition.keyframes == null || definition.keyframes.isEmpty()) {
            throw new IllegalArgumentException(
                objectName + ".keyframeAnimation.keyframes must contain at least one keyframe"
            );
        }

        List<TransformKeyframe> keyframes = definition.keyframes.stream()
            .map(KeyframeAnimationComponentCodec::toTransformKeyframe)
            .toList();

        return new KeyframeAnimationComponent(keyframes)
            .setLoop(definition.loop == null || definition.loop);
    }

    @Override
    public ComponentDefinition write(KeyframeAnimationComponent component) {
        ComponentDefinition definition = new ComponentDefinition();
        definition.type = type();
        definition.loop = component.isLoop();
        definition.keyframes = component.getKeyframes()
            .stream()
            .map(KeyframeAnimationComponentCodec::createKeyframeDefinition)
            .toList();

        return definition;
    }

    private static TransformKeyframe toTransformKeyframe(KeyframeDefinition keyframe) {
        return new TransformKeyframe(
            keyframe.time,
            optionalVec3(keyframe.position, "animation keyframe.position"),
            optionalVec3(keyframe.rotationDegrees, "animation keyframe.rotationDegrees"),
            optionalVec3(keyframe.scale, "animation keyframe.scale")
        );
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
}
