package com.crystal.engine.scene.animation;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KeyframeAnimationComponent extends SceneComponent {

    private final List<TransformKeyframe> keyframes = new ArrayList<>();

    private boolean loop = true;
    private double time;

    public KeyframeAnimationComponent(List<TransformKeyframe> keyframes) {
        if (keyframes == null || keyframes.isEmpty())
            throw new IllegalArgumentException("Animation must contain at least one keyframe");

        this.keyframes.addAll(keyframes);
        this.keyframes.sort(Comparator.comparingDouble(TransformKeyframe::time));
    }

    public KeyframeAnimationComponent setLoop(Boolean loop) {
        this.loop = loop;
        return this;
    }

    @Override
    public void update(SceneUpdateContext context) {
        if (keyframes.size() == 1) {
            apply(keyframes.getFirst());
            return;
        }

        time += context.getDeltaTime();

        double duration = keyframes.getLast().time();

        if (loop && duration > 0.0) {
            time %= duration;
        } else if (time > duration) {
            time = duration;
        }

        TransformKeyframe previous = keyframes.getFirst();
        TransformKeyframe next = keyframes.getLast();

        for (int i = 0; i < keyframes.size(); i++) {
            TransformKeyframe a = keyframes.get(i);
            TransformKeyframe b = keyframes.get(i + 1);

            if (time >= a.time() && time <= b.time()) {
                previous = a;
                next = b;
                break;
            }
        }

        double span = next.time() - previous.time();
        float alpha = span <= 0.0 ? 0.0f : (float) ((time - previous.time()) / span);

        applyInterpolated(previous, next, alpha);
    }

    private void applyInterpolated(TransformKeyframe previous, TransformKeyframe next, float alpha) {
        if (previous.position() != null && next.position() != null) {
            Vector3f position = new Vector3f(previous.position()).lerp(next.position(), alpha);
            getOwner().getTransform().setPosition(position.x, position.y, position.z);
        }

        if (previous.rotationDegrees() != null && next.rotationDegrees() != null) {
            Vector3f rotation = new Vector3f(previous.rotationDegrees()).lerp(next.rotationDegrees(), alpha);
            getOwner().getTransform().setRotationDegrees(rotation.x, rotation.y, rotation.z);
        }

        if (previous.scale() != null && next.scale() != null) {
            Vector3f scale = new Vector3f(previous.scale()).lerp(next.scale(), alpha);
            getOwner().getTransform().setScale(scale.x, scale.y, scale.z);
        }
    }

    private void apply(TransformKeyframe keyframe) {
        if (keyframe.position() != null)
            getOwner().getTransform().setPosition(
                keyframe.position().x,
                keyframe.position().y,
                keyframe.position().z
            );

        if (keyframe.rotationDegrees() != null)
            getOwner().getTransform().setRotationDegrees(
                keyframe.rotationDegrees().x,
                keyframe.rotationDegrees().y,
                keyframe.rotationDegrees().z
            );

        if (keyframe.scale() != null)
            getOwner().getTransform().setScale(
                keyframe.scale().x,
                keyframe.scale().y,
                keyframe.scale().z
            );
    }
}
