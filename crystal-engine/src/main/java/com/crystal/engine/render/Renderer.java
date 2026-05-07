package com.crystal.engine.render;

import com.crystal.engine.render.commands.ClearCommand;
import com.crystal.engine.render.commands.DrawSceneObjectCommand;
import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Scene;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL46.*;

public class Renderer {

    private final RendererConfig config;

    private final RenderContext context = new RenderContext();

    private final RenderQueue queue = new RenderQueue();

    private boolean frustumCullingEnabled;

    public Renderer(RendererConfig config) {
        if (config == null) throw new IllegalArgumentException("RendererConfig cannot be null");

        this.config = config;
        this.frustumCullingEnabled = config.isFrustumCulling();
    }

    public Renderer() {
        this(new RendererConfig());
    }

    public void init(int width, int height) {
        resizeViewport(width, height);

        if (config.isDepthTest()) {
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);
        } else {
            glDisable(GL_DEPTH_TEST);
        }

        if (config.isFaceCulling()) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            glFrontFace(GL_CCW);
        } else {
            glDisable(GL_CULL_FACE);
        }
    }

    // Called at start of frame
    public void beginFrame() {
        queue.clear();
        context.beginFrame();

        queue.submit(new ClearCommand(0.1f, 0.1f, 0.15f, 1.0f));
    }

    // Called at end of frame
    public void renderFrame() {
        queue.execute(context);
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

    public void render(Scene scene, float aspectRatio) {
        beginFrame();

        var camera = scene.getCamera();
        camera.updateFrustum(aspectRatio);
        context.bindScene(scene, aspectRatio);

        List<SceneObject> visibleObjects = new ArrayList<>();

        for (SceneObject root : scene.getRootObjects())
            collectVisibleObjects(root, visibleObjects, camera);

        visibleObjects.sort(Comparator.
                comparingInt((SceneObject object) ->
                        object.getMaterial().getRenderState().getSortKey()
                )
                .thenComparingInt(object ->
                        object.getMaterial().getShaderProgram().getId()
                )
                .thenComparingInt(object ->
                        object.getMaterial().getId()
                )
                .thenComparingInt(object ->
                        object.getMesh().getId()
                )
        );

        for (SceneObject object : visibleObjects)
            queue.submit(new DrawSceneObjectCommand(object, scene, aspectRatio));

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
