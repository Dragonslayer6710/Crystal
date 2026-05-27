package com.crystal.engine.scene.collision;

import com.crystal.engine.scene.Transform;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriggerVolumeTest {

    @Test
    void containsUsesTransformWorldPositionAsCenter() {
        Transform parent = new Transform().setPosition(2.0f, 0.0f, 0.0f);
        Transform child = new Transform().setPosition(1.0f, 1.0f, 1.0f);
        child.setParent(parent);

        TriggerVolume trigger = new TriggerVolume(1.0f, 2.0f, 3.0f);

        assertTrue(trigger.contains(new Vector3f(3.5f, 2.0f, 4.0f), child));
        assertFalse(trigger.contains(new Vector3f(4.1f, 2.0f, 4.0f), child));
    }

    @Test
    void halfExtentsAreDefensiveCopies() {
        TriggerVolume trigger = new TriggerVolume(1.0f, 2.0f, 3.0f);

        Vector3f halfExtents = trigger.getHalfExtents();
        halfExtents.set(10.0f, 10.0f, 10.0f);

        assertTrue(trigger.contains(new Vector3f(1.0f, 2.0f, 3.0f), new Transform()));
        assertFalse(trigger.contains(new Vector3f(2.0f, 0.0f, 0.0f), new Transform()));
    }

    @Test
    void rejectsInvalidHalfExtentsAndContainsArguments() {
        assertThrows(IllegalArgumentException.class, () -> new TriggerVolume(0.0f, 1.0f, 1.0f));
        assertThrows(IllegalArgumentException.class, () -> new TriggerVolume(1.0f, -1.0f, 1.0f));
        assertThrows(IllegalArgumentException.class, () -> new TriggerVolume(1.0f, 1.0f, Float.NaN));

        TriggerVolume trigger = new TriggerVolume();

        assertThrows(IllegalArgumentException.class, () -> trigger.contains(null, new Transform()));
        assertThrows(IllegalArgumentException.class, () -> trigger.contains(new Vector3f(), null));
    }
}
