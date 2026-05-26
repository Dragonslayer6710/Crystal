package com.crystal.engine.render.culling;

import com.crystal.engine.scene.camera.Camera;
import com.crystal.engine.scene.Scene;
import com.crystal.engine.scene.SceneObject;

import java.util.ArrayList;
import java.util.List;

public final class VisibilityCollector {

    private VisibilityCollector() {
    }

    public static Result collect(Scene scene, boolean frustumCullingEnabled, int visibleLayerMask) {
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");
        if (visibleLayerMask == 0) throw new IllegalArgumentException("Visible layer mask cannot be 0");

        Result result = new Result();
        Camera camera = scene.getCamera();

        for (SceneObject root : scene.getRootObjects())
            collect(root, result, camera, frustumCullingEnabled, visibleLayerMask);

        return result;
    }

    private static void collect(SceneObject object, Result result, Camera camera,
                                boolean frustumCullingEnabled, int visibleLayerMask) {
        if (!object.isActive())
            return;

        if (object.isVisible() && object.isRenderable() && isVisibleInLayerMask(object, visibleLayerMask)) {
            result.renderableObjectCount++;
            if (!frustumCullingEnabled || camera.canSee(
                    object.getWorldBoundsCenter(),
                    object.getWorldBoundingRadius()
            )) {
                result.visibleObjects.add(object);
            }
        }

        for (SceneObject child : object.getChildren())
            collect(child, result, camera, frustumCullingEnabled, visibleLayerMask);
    }

    private static boolean isVisibleInLayerMask(SceneObject object, int visibleLayerMask) {
        return (object.getLayerMask() & visibleLayerMask) != 0;
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
