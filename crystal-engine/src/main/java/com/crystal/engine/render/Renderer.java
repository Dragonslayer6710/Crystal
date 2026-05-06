package com.crystal.engine.render;

import com.crystal.engine.render.commands.DrawSceneObjectCommand;
import com.crystal.engine.render.material.RenderState;
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

    public void render(Scene scene, float aspectRatio) {
        beginFrame();

        var camera = scene.getCamera();
        camera.updateFrustum(aspectRatio);

        int submitted = 0;
        int culled = 0;
        int hidden = 0;

        List<SceneObject> visibleObjects = new ArrayList<>();

        for (SceneObject object : scene.getRenderables()) {
            if (!object.isVisible()) {
                hidden++;
                continue;
            }

            if (frustumCullingEnabled && !camera.canSee(
                    object.getTransform().getWorldPosition(),
                    object.getBoundingRadius()
            )) {
                culled++;
                continue;
            }

            visibleObjects.add(object);
        }

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
            submitted++;
        }

        renderFrame();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Render summary: submitted={}, culled={}, hidden={}",
                    submitted,
                    culled,
                    hidden
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
