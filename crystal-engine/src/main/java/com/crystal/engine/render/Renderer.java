package com.crystal.engine.render;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.core.ResourceManager;
import com.crystal.engine.render.commands.*;
import com.crystal.engine.render.gl.RenderPass;
import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.shadow.ShadowMap;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL46.*;

public class Renderer implements Disposable {

    private static final Logger logger = LoggerFactory.getLogger(Renderer.class);

    private final RendererConfig config;
    private final RenderContext context;
    private final ShadowMap directionalShadowMap;

    private final RenderQueue mainQueue = new RenderQueue();
    private final RenderQueue shadowQueue = new RenderQueue();
    private final RenderStats stats = new RenderStats();
    private final VisibilityResult visibilityResult = new VisibilityResult();

    private int viewportWidth;
    private int viewportHeight;
    private boolean frustumCullingEnabled;
    private float exposure = 1.0f;
    private final Vector4f clearColor = new Vector4f(0.1f, 0.1f, 0.15f, 1.0f);
    private int debugViewMode = 0;

    public Renderer(RendererConfig config, ResourceManager resourceManager) {
        if (config == null) throw new IllegalArgumentException("RendererConfig cannot be null");

        this.config = config;
        this.frustumCullingEnabled = config.isFrustumCulling();

        this.directionalShadowMap = new ShadowMap(config.getShadowMapSize());

        this.context = new RenderContext(resourceManager, directionalShadowMap);
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

        glEnable(GL_FRAMEBUFFER_SRGB);
    }

    @Override
    public void dispose() {
        directionalShadowMap.dispose();
    }

    public void render(Scene scene, float aspectRatio) {
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");

        if (aspectRatio <= 0.0f || Float.isNaN(aspectRatio) || Float.isInfinite(aspectRatio))
            throw new IllegalArgumentException("Invalid aspect ratio: " + aspectRatio);

        beginFrame();

        scene.getCamera().updateFrustum(aspectRatio);
        context.prepareScene(scene, aspectRatio);

        List<SceneObject> visibleObjects = collectVisibleObjects(scene);
        sortVisibleObjects(visibleObjects);

        if (config.isShadowsEnabled()) {
            List<SceneObject> shadowCasters = collectShadowCasters(scene);

            buildShadowQueue(scene, shadowCasters);
            executeShadowPass();
        }

        buildMainQueue(scene, visibleObjects);
        executeMainPass();
    }

    public void resizeViewport(int width, int height) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Viewport size must be greater than 0");

        viewportWidth = width;
        viewportHeight = height;

        glViewport(0, 0, width, height);
    }

    public void setFrustumCullingEnabled(boolean enabled) {
        frustumCullingEnabled = enabled;
    }

    public void setExposure(float exposure) {
        if (exposure <= 0.0f) throw new IllegalArgumentException("Exposure must be greater than 0");

        this.exposure = exposure;
    }

    public void setClearColor(float r, float g, float b, float a) {
        if (isInvalidColorChannel(r) ||
                isInvalidColorChannel(g) ||
                isInvalidColorChannel(b) ||
                isInvalidColorChannel(a)) {
            throw new IllegalArgumentException(
                    "Clear color channels must be finite values between 0 and 1"
            );
        }

        clearColor.set(r, g, b, a);
    }

    public void setDebugViewMode(int debugViewMode) {
        if (debugViewMode < 0 || debugViewMode > 10)
            throw new IllegalArgumentException("Debug view mode must be between 0 and 10");

        if (this.debugViewMode == debugViewMode)
            return;

        this.debugViewMode = debugViewMode;

        logger.info("Debug View: {}", getDebugViewName());
    }

    public void cycleDebugViewMode() {
        setDebugViewMode((debugViewMode + 1) % 11);
    }

    public boolean isFrustumCullingEnabled() {
        return frustumCullingEnabled;
    }

    public float getExposure() {
        return exposure;
    }

    public int getDebugViewMode() {
        return debugViewMode;
    }

    public String getDebugViewName() {
        return switch (debugViewMode) {
            case 0 -> "Final";
            case 1 -> "Albedo";
            case 2 -> "Normals";
            case 3 -> "Metallic";
            case 4 -> "Roughness";
            case 5 -> "Ambient Occlusion";
            case 6 -> "Emissive";
            case 7 -> "Irradiance";
            case 8 -> "Prefilter";
            case 9 -> "BRDF LUT";
            case 10 -> "Shadow";
            default -> "Unknown";
        };
    }

    public RenderStats getStats() {
        return stats;
    }

    private void beginFrame() {
        stats.reset();
        mainQueue.clear();
        shadowQueue.clear();

        context.beginFrame();
        context.setDebugViewMode(debugViewMode);
        context.setExposure(exposure);
        context.setShadowsEnabled(config.isShadowsEnabled());
    }

    private List<SceneObject> collectVisibleObjects(Scene scene) {
        visibilityResult.reset();

        Camera camera = scene.getCamera();

        for (SceneObject root : scene.getRootObjects())
            collectVisibleObjects(root, visibilityResult, camera);

        stats.setRenderableObjectCount(visibilityResult.renderableObjectCount);
        stats.setVisibleObjectCount(visibilityResult.visibleObjects.size());
        stats.setCulledObjectCount(Math.max(
                0,
                visibilityResult.renderableObjectCount - visibilityResult.visibleObjects.size()
        ));

        return visibilityResult.visibleObjects;
    }

    private void collectVisibleObjects(SceneObject object, VisibilityResult result, Camera camera) {
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
            collectVisibleObjects(child, result, camera);
    }

    private void sortVisibleObjects(List<SceneObject> visibleObjects) {
        visibleObjects.sort(Comparator.
                comparingInt((SceneObject object) ->
                        object.getMaterial().getRenderState().getSortKey()
                )
                .thenComparingInt(object ->
                        object.getMaterial().getShader().getId()
                )
                .thenComparingInt(object ->
                        object.getMaterial().getId()
                )
                .thenComparingInt(object ->
                        object.getMesh().getId()
                )
        );
    }

    private List<SceneObject> collectShadowCasters(Scene scene) {
        List<SceneObject> casters = new ArrayList<>();

        for (SceneObject root : scene.getRootObjects())
            collectShadowCasters(root, casters);

        return casters;
    }

    private void collectShadowCasters(SceneObject object, List<SceneObject> casters) {
        if (!object.isActive())
            return;

        if (object.isVisible() && object.isRenderable() && object.castsShadow())
            casters.add(object);

        for (SceneObject child : object.getChildren())
            collectShadowCasters(child, casters);
    }

    private void buildShadowQueue(Scene scene, List<SceneObject> shadowCasters) {
        Shader shadowShader = context.getResources().getShadowShader();
        Matrix4f lightSpace = scene.getDirectionalLight().getLightSpaceMatrix();

        for (SceneObject object : shadowCasters) {
            submitShadowCommand(new DrawShadowCommand(object, shadowShader, lightSpace));
            stats.incrementShadowDrawCount();
        }
    }

    private void executeShadowPass() {
        int size = directionalShadowMap.getSize();

        try (RenderPass ignored = new RenderPass(directionalShadowMap.getFramebuffer(), size, size)) {
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);
            glDepthMask(true);
            glDisable(GL_CULL_FACE);
            glColorMask(false, false, false, false);

            glClear(GL_DEPTH_BUFFER_BIT);

            shadowQueue.execute(context);
        }
    }

    private void buildMainQueue(Scene scene, List<SceneObject> visibleObjects) {
        submitMainCommand(new ClearCommand(
                clearColor.x,
                clearColor.y,
                clearColor.z,
                clearColor.w
        ));

        submitSkyboxCommand(scene);
        submitVisibleObjectCommands(visibleObjects);
    }

    private void executeMainPass() {
        try (RenderPass ignored = new RenderPass(viewportWidth, viewportHeight)) {
            mainQueue.execute(context);
        }
    }

    private void submitSkyboxCommand(Scene scene) {
        if (!scene.getEnvironment().hasSkybox())
            return;

        var skyboxShader = context.getResources().getSkyboxShader();
        var skyboxCubeMesh = context.getResources().getSkyboxCubeMesh();

        if (skyboxShader == null || skyboxCubeMesh == null) {
            logger.warn("Scene has skybox, but renderer skybox resources are not set");
            return;
        }

        submitMainCommand(new DrawSkyboxCommand(
                scene,
                skyboxShader,
                skyboxCubeMesh
        ));

        stats.incrementSkyboxDrawCommandCount();
    }

    private void submitVisibleObjectCommands(List<SceneObject> visibleObjects) {
        for (SceneObject object : visibleObjects) {
            submitMainCommand(new DrawSceneObjectCommand(object));
            stats.incrementSceneDrawCommandCount();
        }
    }

    private void submitMainCommand(RenderCommand command) {
        mainQueue.submit(command);
        stats.incrementSubmittedCommandCount();
    }

    private void submitShadowCommand(RenderCommand command) {
        shadowQueue.submit(command);
    }

    private boolean isInvalidColorChannel(float value) {
        return !Float.isFinite(value) ||
                value < 0.0f ||
                value > 1.0f;
    }

    private static final class VisibilityResult {

        private final List<SceneObject> visibleObjects = new ArrayList<>();
        private int renderableObjectCount;

        private void reset() {
            visibleObjects.clear();
            renderableObjectCount = 0;
        }
    }
}
