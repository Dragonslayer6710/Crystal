package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;
import com.crystal.engine.scene.camera.Camera;

public class CameraComponent extends SceneComponent {

    private final Camera camera;

    public CameraComponent(Camera camera) {
        if (camera == null) throw new IllegalArgumentException("Camera cannot be null");
        this.camera = camera;
    }

    public Camera getCamera() {
        return camera;
    }

    @Override
    public void update(SceneUpdateContext context) {
        var owner = getOwner();

        if (owner == null)
            return;

        var ownerTransform = owner.getTransform();
        var cameraTransform = camera.getTransform();

        var position = ownerTransform.getWorldPosition();
        cameraTransform.setPosition(position.x, position.y, position.z);

        cameraTransform.setRotation(ownerTransform.getWorldRotationQuat());
    }
}
