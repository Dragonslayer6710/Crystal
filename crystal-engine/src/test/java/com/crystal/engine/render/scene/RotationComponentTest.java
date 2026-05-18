package com.crystal.engine.render.scene;

import com.crystal.engine.input.Input;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowConfig;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RotationComponentTest {

    @Test
    void updateRotatesOwnerBySpeedAndDeltaTime() {
        SceneObject object = new SceneObject("object", null, null, new Transform());
        RotationComponent component = new RotationComponent(1.0f, 2.0f, 3.0f);

        object.addComponent(component);

        component.update(context(0.5));

        assertVectorEquals(new Vector3f(0.5f, 1.0f, 1.5f), object.getTransform().getRotation());
    }

    @Test
    void setSpeedChangesRotationSpeed() {
        SceneObject object = new SceneObject("object", null, null, new Transform());
        RotationComponent component = new RotationComponent(0.0f, 0.0f, 0.0f)
                .setSpeed(2.0f, 0.0f, 0.0f);

        object.addComponent(component);

        component.update(context(0.25));

        assertVectorEquals(new Vector3f(0.5f, 0.0f, 0.0f), object.getTransform().getRotation());
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
