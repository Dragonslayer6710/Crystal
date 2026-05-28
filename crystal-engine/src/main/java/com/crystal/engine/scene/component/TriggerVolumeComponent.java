package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.collision.TriggerVolume;
import org.joml.Vector3f;

public class TriggerVolumeComponent extends SceneComponent {

    private final TriggerVolume triggerVolume = new TriggerVolume();

    public TriggerVolumeComponent() {
    }

    public TriggerVolumeComponent(float halfWidth, float halfHeight, float halfDepth) {
        setHalfExtents(halfWidth, halfHeight, halfDepth);
    }

    public TriggerVolume getTriggerVolume() {
        return triggerVolume;
    }

    public Vector3f getHalfExtents() {
        return triggerVolume.getHalfExtents();
    }

    public TriggerVolumeComponent setHalfExtents(float halfWidth, float halfHeight, float halfDepth) {
        triggerVolume.setHalfExtents(halfWidth, halfHeight, halfDepth);
        return this;
    }

    public boolean contains(Vector3f point) {
        if (getOwner() == null)
            return false;

        return triggerVolume.contains(point, getOwner().getTransform());
    }
}
