package com.crystal.engine.render;

import com.crystal.engine.render.commands.DrawSceneObjectCommand;
import com.crystal.engine.render.material.RenderState;
import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Scene;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL46.*;

public class Renderer {

    private final RenderQueue queue = new RenderQueue();

    private boolean frustumCullingEnabled = true;

    private boolean currentDepthTest = true;
    private boolean currentCullFace = true;
    private boolean currentWireframe = false;

    private int currentShaderId = 0;

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

    private void collectVisibleObjects(SceneObject object, List<SceneObject> visibleObjects, Camera camera) {
        if (!object.isActive())
            return;

        if (!object.isVisible()) {
            return;
        }

        if (!frustumCullingEnabled || camera.canSee(
                object.getTransform().getWorldPosition(),
                object.getBoundingRadius()
        ))
            visibleObjects.add(object);

        for (SceneObject child : object.getChildren())
            collectVisibleObjects(child, visibleObjects, camera);
    }

    private void submitSceneObjects(List<SceneObject> visibleObjects, Scene scene, float aspectRatio) {
        for (SceneObject object : visibleObjects) {
            applyRenderState(object.getMaterial().getRenderState());

            queue.submit(new DrawSceneObjectCommand(object, scene, aspectRatio));
        }
    }

    public void render(Scene scene, float aspectRatio) {
        beginFrame();

        var camera = scene.getCamera();
        camera.updateFrustum(aspectRatio);

        List<SceneObject> visibleObjects = new ArrayList<>();

        for (SceneObject root : scene.getRootObjects())
            collectVisibleObjects(root, visibleObjects, camera);

        visibleObjects.sort(Comparator.
                comparingInt((SceneObject object) ->
                        object.getMaterial().getRenderState().getSortKey()
                )
                .thenComparingInt(object ->
                        object.getMaterial().getId()
                ).thenComparingInt(object ->
                        object.getMaterial().getShaderProgram().getId()
                )
        );

        submitSceneObjects(visibleObjects, scene, aspectRatio);

        renderFrame();
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
