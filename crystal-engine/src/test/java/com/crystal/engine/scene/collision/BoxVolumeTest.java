package com.crystal.engine.scene.collision;

import com.crystal.engine.scene.Transform;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoxVolumeTest {

    @Test
    void containsUsesTransformWorldPositionAsCenter() {
        Transform parent = new Transform().setPosition(2.0f, 0.0f, 0.0f);
        Transform child = new Transform().setPosition(1.0f, 1.0f, 1.0f);
        child.setParent(parent);

        BoxVolume volume = new BoxVolume(1.0f, 2.0f, 3.0f);

        assertTrue(volume.contains(new Vector3f(3.5f, 2.0f, 4.0f), child));
        assertFalse(volume.contains(new Vector3f(4.1f, 2.0f, 4.0f), child));
    }

    @Test
    void intersectsOverlappingVolumes() {
        BoxVolume first = new BoxVolume(1.0f, 1.0f, 1.0f);
        BoxVolume second = new BoxVolume(0.5f, 0.5f, 0.5f);

        Transform firstTransform = new Transform().setPosition(0.0f, 0.0f, 0.0f);
        Transform touchingTransform = new Transform().setPosition(1.5f, 0.0f, 0.0f);
        Transform separatedTransform = new Transform().setPosition(1.6f, 0.0f, 0.0f);

        assertTrue(first.intersects(second, firstTransform, touchingTransform));
        assertFalse(first.intersects(second, firstTransform, separatedTransform));
    }

    @Test
    void halfExtentsAreDefensiveCopies() {
        BoxVolume volume = new BoxVolume(1.0f, 2.0f, 3.0f);

        Vector3f halfExtents = volume.getHalfExtents();
        halfExtents.set(10.0f, 10.0f, 10.0f);

        assertTrue(volume.contains(new Vector3f(1.0f, 2.0f, 3.0f), new Transform()));
        assertFalse(volume.contains(new Vector3f(2.0f, 0.0f, 0.0f), new Transform()));
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new BoxVolume(0.0f, 1.0f, 1.0f));
        assertThrows(IllegalArgumentException.class, () -> new BoxVolume(1.0f, -1.0f, 1.0f));
        assertThrows(IllegalArgumentException.class, () -> new BoxVolume(1.0f, 1.0f, Float.NaN));

        BoxVolume volume = new BoxVolume();

        assertThrows(IllegalArgumentException.class, () -> volume.contains(null, new Transform()));
        assertThrows(IllegalArgumentException.class, () -> volume.contains(new Vector3f(), null));
        assertThrows(IllegalArgumentException.class, () -> volume.intersects(null, new Transform(), new Transform()));
        assertThrows(IllegalArgumentException.class, () -> volume.intersects(new BoxVolume(), null, new Transform()));
        assertThrows(IllegalArgumentException.class, () -> volume.intersects(new BoxVolume(), new Transform(), null));
    }
}
