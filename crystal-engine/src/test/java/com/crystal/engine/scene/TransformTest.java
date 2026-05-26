package com.crystal.engine.scene;

import com.crystal.engine.scene.Transform;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransformTest {

    @Test
    void getPositionReturnsCopy() {
        Transform transform = new Transform().setPosition(1, 2, 3);

        Vector3f position = transform.getPosition();
        position.set(9, 9, 9);

        assertEquals(new Vector3f(1, 2, 3), transform.getPosition());
    }

    @Test
    void translateUpdatesWorldPosition() {
        Transform transform = new Transform().setPosition(1, 2, 3);

        transform.translate(4, 5, 6);

        assertEquals(new Vector3f(5, 7, 9), transform.getWorldPosition());
    }

    @Test
    void childWorldPositionIncludesParentPosition() {
        Transform parent = new Transform().setPosition(10, 0, 0);
        Transform child = new Transform().setPosition(1, 2, 3);
        child.setParent(parent);

        assertEquals(new Vector3f(11, 2, 3), child.getWorldPosition());
    }

    @Test
    void getRotationReturnsCopy() {
        Transform transform = new Transform().setRotation(0.1f, 0.2f, 0.3f);

        Vector3f rotation = transform.getRotation();
        rotation.set(9.0f, 9.0f, 9.0f);

        assertVectorEquals(new Vector3f(0.1f, 0.2f, 0.3f), transform.getRotation());
    }

    @Test
    void setRotationAffectsLocalMatrix() {
        Transform transform = new Transform()
                .setRotation(0.0f, (float) Math.toRadians(90.0), 0.0f);

        Vector3f transformedForward = transform.getLocalMatrix()
                .transformDirection(new Vector3f(0.0f, 0.0f, -1.0f));

        assertVectorEquals(new Vector3f(-1.0f, 0.0f, 0.0f), transformedForward);
    }

    @Test
    void rotateAffectsLocalMatrix() {
        Transform transform = new Transform()
                .rotate(0.0f, (float) Math.toRadians(90.0), 0.0f);

        Vector3f transformedForward = transform.getLocalMatrix()
                .transformDirection(new Vector3f(0.0f, 0.0f, -1.0f));

        assertVectorEquals(new Vector3f(-1.0f, 0.0f, 0.0f), transformedForward);
    }

    private static void assertVectorEquals(Vector3f expected, Vector3f actual) {
        assertTrue(
                expected.distance(actual) < 0.0001f,
                "Expected " + expected + " but was " + actual
        );
    }
}
