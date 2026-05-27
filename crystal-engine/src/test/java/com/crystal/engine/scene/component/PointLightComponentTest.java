package com.crystal.engine.scene.component;

import com.crystal.engine.input.Input;
import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.scene.SceneUpdateContext;
import com.crystal.engine.scene.Transform;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowConfig;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PointLightComponentTest {

    @Test
    void updateCopiesOwnerWorldPositionToLight() {
        SceneObject parent = new SceneObject("parent", null, null, new Transform());
        SceneObject child = new SceneObject("child", null, null, new Transform());
        PointLightComponent component = new PointLightComponent();

        parent.getTransform().setPosition(2.0f, 3.0f, 4.0f);
        child.getTransform().setPosition(1.0f, 0.5f, -2.0f);
        parent.addChild(child);
        child.addComponent(component);

        component.update(context(0.0));

        assertVectorEquals(new Vector3f(3.0f, 3.5f, 2.0f), component.getLight().getPosition());
    }

    @Test
    void settersUpdateLightProperties() {
        PointLightComponent component = new PointLightComponent()
            .setColor(0.25f, 0.5f, 0.75f)
            .setIntensity(2.0f)
            .setRadius(8.0f);

        assertVectorEquals(new Vector3f(0.25f, 0.5f, 0.75f), component.getLight().getColor());
        assertEquals(2.0f, component.getLight().getIntensity());
        assertEquals(8.0f, component.getLight().getRadius());
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
