package com.crystal.engine.render;

import com.crystal.engine.render.commands.DrawSceneObjectCommand;
import com.crystal.engine.render.material.RenderState;
import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL46.*;

public class Renderer {

    private static final Logger logger = LoggerFactory.getLogger(Renderer.class);

    private static class RenderStats {
        int active;
        int hidden;
        int culled;
        int submitted;
    }

    private final RenderQueue queue = new RenderQueue();

    private boolean frustumCullingEnabled = true;

    private boolean currentDepthTest = true;
    private boolean currentCullFace = true;
    private boolean currentWireframe = false;

    public void init(int width, int height) {
        resizeViewport(width, height);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
    }

    // Called at start of frame
    public void beginFrame() {
        queue.clear();
        glClearColor(0.1f, 0.1f, 0.15f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    // Called at end of frame
    public void renderFrame() {
        queue.execute();
    }

    public void applyRenderState(RenderState state) {
        if (state.isDepthTest() != currentDepthTest) {
            currentDepthTest = state.isDepthTest();

            if (currentDepthTest) {
                glEnable(GL_DEPTH_TEST);
            } else {
                glDisable(GL_DEPTH_TEST);
            }
        }

        if (state.isCullFace() != currentCullFace) {
            currentCullFace = state.isCullFace();

            if (currentCullFace) {
                glEnable(GL_CULL_FACE);
            } else {
                glDisable(GL_CULL_FACE);
            }
        }

        if (state.isWireframe() != currentWireframe) {
            currentWireframe = state.isWireframe();

            glPolygonMode(
                    GL_FRONT_AND_BACK,
                    currentWireframe ? GL_LINE : GL_FILL
            );
        }
    }

    private int countSubtree(SceneObject object) {
        int count = 1;

        for (SceneObject child : object.getChildren())
            count += countSubtree(child);

        return count;
    }

    private void collectVisibleObjects(
            SceneObject object,
            List<SceneObject> visibleObjects,
            Camera camera,
            RenderStats stats
    ) {
        if (!object.isActive())
            return;
        stats.active++;

        if (!object.isVisible()) {
            stats.hidden += countSubtree(object);
            return;
        } else {
            boolean visible = !frustumCullingEnabled || camera.canSee(
                    object.getTransform().getWorldPosition(),
                    object.getBoundingRadius()
            );

            if (visible) {
                visibleObjects.add(object);
            } else {
                stats.culled++;
            }
        }

        for (SceneObject child : object.getChildren())
            collectVisibleObjects(child, visibleObjects, camera, stats);
    }

    public void render(Scene scene, float aspectRatio) {
        beginFrame();

        var camera = scene.getCamera();
        camera.updateFrustum(aspectRatio);

        RenderStats stats = new RenderStats();

        List<SceneObject> visibleObjects = new ArrayList<>();

        for (SceneObject root : scene.getRootObjects())
            collectVisibleObjects(root, visibleObjects, camera, stats);

        visibleObjects.sort(Comparator.
                comparingInt((SceneObject object) ->
                        object.getMaterial().getRenderState().hashCode()
                )
                .thenComparingInt(object ->
                        object.getMaterial().hashCode()
                )
        );

        for (SceneObject object : visibleObjects) {
            applyRenderState(object.getMaterial().getRenderState());

            queue.submit(new DrawSceneObjectCommand(object, scene, aspectRatio));
            stats.submitted++;
        }

        renderFrame();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Render summary: active={}, submitted={}, culled={}, hidden={}",
                    stats.active,
                    stats.submitted,
                    stats.culled,
                    stats.hidden
            );
        }
    }

    public void resizeViewport(int width, int height) {
        glViewport(0, 0, width, height);
    }

    public boolean isFrustumCullingEnabled() {
        return frustumCullingEnabled;
    }

    public void setFrustumCullingEnabled(boolean enabled) {
        frustumCullingEnabled = enabled;
    }
}
