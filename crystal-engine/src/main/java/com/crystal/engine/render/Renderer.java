package com.crystal.engine.render;

import com.crystal.engine.render.commands.ClearCommand;
import com.crystal.engine.render.commands.DrawSceneObjectCommand;
import com.crystal.engine.render.commands.DrawSkyboxCommand;
import com.crystal.engine.render.commands.RenderCommand;
import com.crystal.engine.render.gl.RenderPass;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.texture.Texture;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL46.*;

public class Renderer {

    private static final Logger logger = LoggerFactory.getLogger(Renderer.class);

    private final RendererConfig config;

    private final RenderContext context = new RenderContext();

    private final RenderQueue queue = new RenderQueue();
    private final RenderStats stats = new RenderStats();
    private final VisibilityResult visibilityResult = new VisibilityResult();

    private int viewportWidth;
    private int viewportHeight;

    private boolean frustumCullingEnabled;

    private float exposure = 1.0f;

    private final Vector4f clearColor = new Vector4f(0.1f, 0.1f, 0.15f, 1.0f);

    private int debugViewMode = 0;

    private Shader skyboxShader;
    private Mesh skyboxCubeMesh;

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

        glEnable(GL_FRAMEBUFFER_SRGB);
    }

    // Called at start of frame
    public void beginFrame() {
        stats.reset();

        queue.clear();
        context.beginFrame();
        context.setDebugViewMode(debugViewMode);
        context.setExposure(exposure);

        submitCommand(new ClearCommand(
                clearColor.x,
                clearColor.y,
                clearColor.z,
                clearColor.w
        ));
    }

    // Called at end of frame
    public void renderFrame() {
        queue.execute(context);
    }

    private void prepareFrame(Scene scene, float aspectRatio) {
        beginFrame();

        var camera = scene.getCamera();

        camera.updateFrustum(aspectRatio);

        context.prepareScene(scene, aspectRatio);
    }

    private void submitSkybox(Scene scene) {
        if (!scene.getEnvironment().hasSkybox())
            return;

        if (skyboxShader == null || skyboxCubeMesh == null) {
            logger.warn("Scene has skybox, but renderer skybox resources are not set");
            return;
        }

        submitCommand(new DrawSkyboxCommand(
                scene,
                skyboxShader,
                skyboxCubeMesh
        ));

        stats.incrementSkyboxDrawCommandCount();
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

    private void submitVisibleObjects(List<SceneObject> visibleObjects) {
        for (SceneObject object : visibleObjects) {
            submitCommand(new DrawSceneObjectCommand(object));
            stats.incrementSceneDrawCommandCount();
        }
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

    private void submitCommand(RenderCommand command) {
        queue.submit(command);
        stats.incrementSubmittedCommandCount();
    }

    public void render(Scene scene, float aspectRatio) {
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");

        if (aspectRatio <= 0.0f || Float.isNaN(aspectRatio) || Float.isInfinite(aspectRatio))
            throw new IllegalArgumentException("Invalid aspect ratio: " + aspectRatio);

        try (RenderPass ignored = new RenderPass(viewportWidth, viewportHeight)) {
            prepareFrame(scene, aspectRatio);
            submitSkybox(scene);
            List<SceneObject> visibleObjects = collectVisibleObjects(scene);
            sortVisibleObjects(visibleObjects);
            submitVisibleObjects(visibleObjects);
            renderFrame();
        }
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

    private boolean isInvalidColorChannel(float value) {
        return !Float.isFinite(value) ||
                value < 0.0f ||
                value > 1.0f;
    }

    public void setDebugViewMode(int debugViewMode) {
        if (debugViewMode < 0 || debugViewMode > 9)
            throw new IllegalArgumentException("Debug view mode must be between 0 and 9");

        if (this.debugViewMode == debugViewMode)
            return;

        this.debugViewMode = debugViewMode;

        logger.info("Debug View: {}", getDebugViewName());
    }

    public void cycleDebugViewMode() {
        setDebugViewMode((debugViewMode + 1) % 10);
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
            default -> "Unknown";
        };
    }

    public RenderStats getStats() {
        return stats;
    }

    public void setDefaultTextures(Texture white, Texture normal, Texture blackCubemap, Texture brdfLut) {
        context.setDefaultTextures(white, normal, blackCubemap, brdfLut);
    }

    public void setResources(RenderResources resources) {
        context.setResources(resources);
    }

    public void setSkyboxResources(Shader shader, Mesh cubeMesh) {
        if (shader == null) throw new IllegalArgumentException("Skybox shader cannot be null");
        if (cubeMesh == null) throw new IllegalArgumentException("Skybox cube mesh cannot be null");

        this.skyboxShader = shader;
        this.skyboxCubeMesh = cubeMesh;
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
