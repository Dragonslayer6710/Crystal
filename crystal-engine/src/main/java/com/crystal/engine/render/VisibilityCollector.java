package com.crystal.engine.render;

import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.render.scene.SceneObject;

import java.util.ArrayList;
import java.util.List;

final class VisibilityCollector {

    private VisibilityCollector() {
    }

    static Result collect(Scene scene, boolean frustumCullingEnabled) {
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

    static final class Result {

        private final List<SceneObject> visibleObjects = new ArrayList<>();
        private int renderableObjectCount;

        List<SceneObject> getVisibleObjects() {
            return visibleObjects;
        }

        int getRenderableObjectCount() {
            return renderableObjectCount;
        }

        int getCulledObjectCount() {
            return Math.max(0, renderableObjectCount - visibleObjects.size());
        }
    }
}
