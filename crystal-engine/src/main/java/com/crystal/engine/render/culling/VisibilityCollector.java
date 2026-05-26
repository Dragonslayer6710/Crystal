package com.crystal.engine.render.culling;

import com.crystal.engine.scene.camera.Camera;
import com.crystal.engine.scene.Scene;
import com.crystal.engine.scene.SceneObject;

import java.util.ArrayList;
import java.util.List;

public final class VisibilityCollector {

    private VisibilityCollector() {
    }

    public static Result collect(Scene scene, boolean frustumCullingEnabled) {
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");

        Result result = new Result();
        Camera camera = scene.getCamera();

        for (SceneObject root : scene.getRootObjects())
            collect(root, result, camera, frustumCullingEnabled);

        return result;
    }

    private static void collect(SceneObject object, Result result, Camera camera, boolean frustumCullingEnabled) {
        if (!object.isActive())
            return;

        if (object.isVisible() && object.isRenderable()) {
            result.renderableObjectCount++;
            if (!frustumCullingEnabled || camera.canSee(
                    object.getWorldBoundsCenter(),
                    object.getWorldBoundingRadius()
            )) {
                result.visibleObjects.add(object);
            }
        }

        for (SceneObject child : object.getChildren())
            collect(child, result, camera, frustumCullingEnabled);
    }

    public static final class Result {

        private final List<SceneObject> visibleObjects = new ArrayList<>();
        private int renderableObjectCount;

        public List<SceneObject> getVisibleObjects() {
            return visibleObjects;
        }

        public int getRenderableObjectCount() {
            return renderableObjectCount;
        }

        public int getCulledObjectCount() {
            return Math.max(0, renderableObjectCount - visibleObjects.size());
        }
    }
}
