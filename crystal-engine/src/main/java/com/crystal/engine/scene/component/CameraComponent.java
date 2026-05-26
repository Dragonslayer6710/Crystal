package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
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
}
