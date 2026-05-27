package com.crystal.engine.scene.collision;

import com.crystal.engine.scene.Transform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoxColliderTest {

    @Test
    void intersectsUsesColliderVolumesAndTransforms() {
        BoxCollider first = new BoxCollider(1.0f, 1.0f, 1.0f);
        BoxCollider second = new BoxCollider(0.5f, 0.5f, 0.5f);

        Transform firstTransform = new Transform().setPosition(0.0f, 0.0f, 0.0f);
        Transform touchingTransform = new Transform().setPosition(1.5f, 0.0f, 0.0f);
        Transform separatedTransform = new Transform().setPosition(1.6f, 0.0f, 0.0f);

        assertTrue(first.intersects(second, firstTransform, touchingTransform));
        assertFalse(first.intersects(second, firstTransform, separatedTransform));
    }

    @Test
    void rejectsNullColliderIntersections() {
        BoxCollider collider = new BoxCollider();

        assertThrows(
            IllegalArgumentException.class,
            () -> collider.intersects(null, new Transform(), new Transform())
        );
    }
}
