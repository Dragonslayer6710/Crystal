package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;

public class CameraLookComponent extends SceneComponent {

    private float mouseSensitivity = 0.002f;

    public CameraLookComponent setMouseSensitivity(float mouseSensitivity) {
        if (!Float.isFinite(mouseSensitivity) || mouseSensitivity < 0.0f)
            throw new IllegalArgumentException("Mouse sensitivity must be finite and non-negative");

        this.mouseSensitivity = mouseSensitivity;
        return this;
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }


    @Override
    public void update(SceneUpdateContext context) {
        if (!context.getWindow().isCursorCaptured())
            return;

        var input = context.getInput();
        var ownerTransform = getOwner().getTransform();
        var rotation = ownerTransform.getRotation();

        rotation.y -= (float) (input.getMouseDeltaX() * mouseSensitivity);
        rotation.x -= (float) (input.getMouseDeltaY() * mouseSensitivity);

        // clamp pitch to not flip upside down
        float maxPitch = (float) Math.toRadians(89.0f);
        rotation.x = Math.clamp(rotation.x, -maxPitch, maxPitch);

        ownerTransform.setRotation(rotation.x, rotation.y, rotation.z);
    }
}
