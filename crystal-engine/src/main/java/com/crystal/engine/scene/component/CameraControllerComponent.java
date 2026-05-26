package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;
import com.crystal.engine.scene.camera.Camera;

public abstract class CameraControllerComponent extends SceneComponent {

    private final Camera camera;

    protected CameraControllerComponent(Camera camera) {
        if (camera == null) throw new IllegalArgumentException("Camera cannot be null");
        this.camera = camera;
    }

    protected Camera getCamera() {
        return camera;
    }

    @Override
    public final void update(SceneUpdateContext context) {
        if (context == null) throw new IllegalArgumentException("SceneUpdateContext cannot be null");
        updateCamera(camera, context);
    }

    protected abstract void updateCamera(Camera camera, SceneUpdateContext context);
}
