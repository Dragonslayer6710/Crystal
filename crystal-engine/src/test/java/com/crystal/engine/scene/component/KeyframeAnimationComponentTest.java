package com.crystal.engine.scene.component;

import com.crystal.engine.input.Input;
import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.scene.SceneUpdateContext;
import com.crystal.engine.scene.Transform;
import com.crystal.engine.scene.animation.TransformKeyframe;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowConfig;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyframeAnimationComponentTest {

    @Test
    void constructorSortsKeyframesByTimeAndExposesCopy() {
        TransformKeyframe later = keyframe(1.0, new Vector3f(1.0f, 0.0f, 0.0f));
        TransformKeyframe earlier = keyframe(0.0, new Vector3f());
        KeyframeAnimationComponent component = new KeyframeAnimationComponent(List.of(later, earlier));

        List<TransformKeyframe> keyframes = component.getKeyframes();

        assertEquals(0.0, keyframes.getFirst().time());
        assertEquals(1.0, keyframes.getLast().time());
        assertThrows(UnsupportedOperationException.class, () -> keyframes.add(later));
    }

    @Test
    void updateInterpolatesBetweenKeyframes() {
        SceneObject object = object();
        KeyframeAnimationComponent component = new KeyframeAnimationComponent(List.of(
            keyframe(0.0, new Vector3f(0.0f, 0.0f, 0.0f)),
            keyframe(1.0, new Vector3f(10.0f, 0.0f, 0.0f))
        ));
        object.addComponent(component);

        component.update(context(0.25));

        assertVectorEquals(new Vector3f(2.5f, 0.0f, 0.0f), object.getTransform().getPosition());
    }

    @Test
    void nonLoopingAnimationClampsToFinalKeyframe() {
        SceneObject object = object();
        KeyframeAnimationComponent component = new KeyframeAnimationComponent(List.of(
            keyframe(0.0, new Vector3f(0.0f, 0.0f, 0.0f)),
            keyframe(1.0, new Vector3f(10.0f, 0.0f, 0.0f))
        )).setLoop(false);
        object.addComponent(component);

        component.update(context(2.0));

        assertVectorEquals(new Vector3f(10.0f, 0.0f, 0.0f), object.getTransform().getPosition());
    }

    @Test
    void singleKeyframeAppliesDirectly() {
        SceneObject object = object();
        KeyframeAnimationComponent component = new KeyframeAnimationComponent(List.of(
            new TransformKeyframe(
                0.0,
                new Vector3f(1.0f, 2.0f, 3.0f),
                new Vector3f(10.0f, 20.0f, 30.0f),
                new Vector3f(2.0f, 3.0f, 4.0f)
            )
        ));
        object.addComponent(component);

        component.update(context(0.5));

        assertVectorEquals(new Vector3f(1.0f, 2.0f, 3.0f), object.getTransform().getPosition());
        assertVectorEquals(
            new Vector3f((float) Math.toRadians(10.0), (float) Math.toRadians(20.0), (float) Math.toRadians(30.0)),
            object.getTransform().getRotation()
        );
        assertVectorEquals(new Vector3f(2.0f, 3.0f, 4.0f), object.getTransform().getScale());
    }

    @Test
    void rejectsMissingKeyframes() {
        assertThrows(IllegalArgumentException.class, () -> new KeyframeAnimationComponent(null));
        assertThrows(IllegalArgumentException.class, () -> new KeyframeAnimationComponent(List.of()));
    }

    private static TransformKeyframe keyframe(double time, Vector3f position) {
        return new TransformKeyframe(time, position, null, null);
    }

    private static SceneObject object() {
        return new SceneObject("object", null, null, new Transform());
    }

    private static void assertVectorEquals(Vector3f expected, Vector3f actual) {
        assertTrue(
            expected.distance(actual) < 0.0001f,
            "Expected " + expected + " but was " + actual
        );
    }

    private static SceneUpdateContext context(double deltaTime) {
        return new SceneUpdateContext(deltaTime, new Input(), new Window(new WindowConfig()));
    }
}
