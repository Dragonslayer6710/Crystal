package com.crystal.engine.scene.component;

import com.crystal.engine.input.Input;
import com.crystal.engine.scene.Scene;
import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.scene.SceneUpdateContext;
import com.crystal.engine.scene.Transform;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowConfig;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrbitComponentTest {

    @Test
    void constructorAppliesRadiusAndSpeed() {
        OrbitComponent component = new OrbitComponent(2.0f, 3.0f);

        assertEquals(2.0f, component.getRadius());
        assertEquals(3.0f, component.getSpeed());
    }

    @Test
    void setCenterCopiesCenterValues() {
        OrbitComponent component = new OrbitComponent()
            .setCenter(1.0f, 2.0f, 3.0f);

        Vector3f center = component.getCenter();
        center.set(9.0f, 9.0f, 9.0f);

        assertVectorEquals(new Vector3f(1.0f, 2.0f, 3.0f), component.getCenter());
    }

    @Test
    void updateOrbitsAroundCenterOnXzPlane() {
        SceneObject object = new SceneObject("object", null, null, new Transform());
        OrbitComponent component = new OrbitComponent(2.0f, 1.0f)
            .setCenter(1.0f, 2.0f, 3.0f)
            .setPhase(0.0f);

        object.addComponent(component);

        component.update(context(Math.PI / 2.0));

        assertVectorEquals(
            new Vector3f(1.0f, 2.0f, 5.0f),
            object.getTransform().getPosition()
        );

        component.update(context(Math.PI / 2.0));

        assertVectorEquals(
            new Vector3f(-1.0f, 2.0f, 3.0f),
            object.getTransform().getPosition()
        );
    }

    @Test
    void settersRejectInvalidValues() {
        OrbitComponent component = new OrbitComponent();

        assertThrows(IllegalArgumentException.class, () -> component.setCenter(Float.NaN, 0.0f, 0.0f));
        assertThrows(IllegalArgumentException.class, () -> component.setRadius(-0.1f));
        assertThrows(IllegalArgumentException.class, () -> component.setRadius(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> component.setSpeed(Float.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> component.setPhase(Float.NaN));
    }

    private static void assertVectorEquals(Vector3f expected, Vector3f actual) {
        assertTrue(
            expected.distance(actual) < 0.0001f,
            "Expected " + expected + " but was " + actual
        );
    }

    private static SceneUpdateContext context(double deltaTime) {
        return new SceneUpdateContext(deltaTime, new Input(), new Window(new WindowConfig()), new Scene());
    }
}

