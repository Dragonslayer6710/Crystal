package com.crystal.engine.debug;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.core.Time;
import com.crystal.engine.render.RenderLayers;
import com.crystal.engine.render.RenderStats;
import com.crystal.engine.render.Renderer;
import com.crystal.engine.scene.Scene;
import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneObject;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

public class DebugOverlay implements Disposable {

    private static final String[] DEBUG_VIEW_NAMES = {
        "Final",
        "Albedo",
        "Normals",
        "Metallic",
        "Roughness",
        "Ambient Occlusion",
        "Emissive",
        "Irradiance",
        "Prefilter",
        "BRDF LUT",
        "Shadow",
        "IBL Specular"
    };

    private final ImGuiImplGlfw glfwBackend = new ImGuiImplGlfw();
    private final ImGuiImplGl3 glBackend = new ImGuiImplGl3();

    private boolean initialised;

    private SceneObject selectedObject;

    public void init(long windowHandle) {
        if (initialised)
            return;

        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        /*
         * ImGui installs GLFW callbacks after the engine callbacks and chains them.
         * If this ever interferes with Input, Window should grow an explicit callback
         * fanout instead of allowing UI code to own callback registration directly.
         */

        glfwBackend.init(windowHandle, true);
        glBackend.init("#version 460 core");

        initialised = true;
    }

    public void render(Time time, Renderer renderer, Scene scene) {
        if (!initialised)
            return;

        glfwBackend.newFrame();
        glBackend.newFrame();
        ImGui.newFrame();

        drawPerformanceWindow(time, renderer, scene);

        ImGui.render();
        glBackend.renderDrawData(ImGui.getDrawData());
    }

    public boolean wantsMouse() {
        return initialised && ImGui.getIO().getWantCaptureMouse();
    }

    public boolean wantsKeyboard() {
        return initialised && ImGui.getIO().getWantCaptureKeyboard();
    }

    private void drawPerformanceWindow(Time time, Renderer renderer, Scene scene) {
        RenderStats stats = renderer.getStats();

        ImGui.begin("Crystal Debug");

        ImGui.text("Performance");
        ImGui.separator();
        ImGui.text("FPS: " + time.getFps());
        ImGui.text(String.format("Frame: %.2f ms", time.getFrameTimeMs()));
        ImGui.text(String.format("Update: %.2f ms", time.getUpdateTimeMs()));
        ImGui.text(String.format("Render: %.2f ms", time.getRenderTimeMs()));

        ImGui.spacing();

        ImGui.text("Renderer");
        ImGui.separator();
        drawDebugViewSelector(renderer);
        drawRendererControls(renderer);
        ImGui.text("Renderable: " + stats.getRenderableObjectCount());
        ImGui.text("Culled: " + stats.getCulledObjectCount());
        ImGui.text("Draws: " + stats.getTotalDrawCount());
        ImGui.text("Submitted Commands: " + stats.getSubmittedCommandCount());

        ImGui.spacing();

        drawSceneSection(scene);

        ImGui.end();
    }

    private void drawRendererControls(Renderer renderer) {
        ImBoolean frustumCulling = new ImBoolean(renderer.isFrustumCullingEnabled());

        if (ImGui.checkbox("Frustum Culling", frustumCulling))
            renderer.setFrustumCullingEnabled(frustumCulling.get());

        ImFloat exposure = new ImFloat(renderer.getExposure());

        if (ImGui.sliderFloat("Exposure", exposure.getData(), 0.1f, 5.0f, "%.2f"))
            renderer.setExposure(exposure.get());

        drawLayerMaskControls(renderer);
    }

    private void drawLayerMaskControls(Renderer renderer) {
        ImGui.spacing();
        ImGui.text("Visible Layers");

        int mask = renderer.getVisibleLayerMask();

        mask = drawLayerCheckbox("World", mask, RenderLayers.WORLD);
        mask = drawLayerCheckbox("UI", mask, RenderLayers.UI);
        mask = drawLayerCheckbox("Debug", mask, RenderLayers.DEBUG);
        mask = drawLayerCheckbox("Editor", mask, RenderLayers.EDITOR);

        if (mask != renderer.getVisibleLayerMask()) {
            renderer.setVisibleLayerMask(mask);
        }
    }

    private int drawLayerCheckbox(String label, int currentMask, int layer) {
        ImBoolean enabled = new ImBoolean((currentMask & layer) != 0);

        if (ImGui.checkbox(label, enabled)) {
            if (enabled.get()) {
                return currentMask | layer;
            }

            int updatedMask = currentMask & ~layer;
            return updatedMask == 0 ? currentMask : updatedMask;
        }

        return currentMask;
    }

    private void drawDebugViewSelector(Renderer renderer) {
        int currentMode = renderer.getDebugViewMode();
        String currentName = renderer.getDebugViewName();

        if (ImGui.beginCombo("Debug View", currentName)) {
            for (int mode = 0; mode < DEBUG_VIEW_NAMES.length; mode++) {
                boolean selected = mode == currentMode;

                if (ImGui.selectable(DEBUG_VIEW_NAMES[mode], selected)) {
                    renderer.setDebugViewMode(mode);
                }
            }

            ImGui.endCombo();
        }
    }

    private void drawSceneSection(Scene scene) {
        ImGui.text("Scene");
        ImGui.separator();
        ImGui.text("Root Objects: " + scene.getRootObjects().size());

        drawEnvironmentControls(scene);
        drawActiveCameraSection(scene);

        if (ImGui.collapsingHeader("Hierarchy")) {
            for (SceneObject root: scene.getRootObjects()) {
                drawSceneObjectNode(root);
            }
        }

        drawSelectedObjectInspector(scene);
    }

    private void drawEnvironmentControls(Scene scene) {
        var environment = scene.getEnvironment();

        ImGui.spacing();
        ImGui.text("Environment");
        ImGui.separator();

        ImFloat iblDiffuseIntensity = new ImFloat(environment.getIblDiffuseIntensity());
        if (ImGui.sliderFloat("IBL Diffuse", iblDiffuseIntensity.getData(), 0.0f, 2.0f, "%.2f")) {
            environment.setIblDiffuseIntensity(iblDiffuseIntensity.get());
        }

        ImFloat iblSpecularIntensity = new ImFloat(environment.getIblSpecularIntensity());
        if (ImGui.sliderFloat("IBL Specular", iblSpecularIntensity.getData(), 0.0f, 2.0f, "%.2f")) {
            environment.setIblSpecularIntensity(iblSpecularIntensity.get());
        }
    }

    private void drawActiveCameraSection(Scene scene) {
        var transform = scene.getCamera().getTransform();

        var position = transform.getPosition();
        var rotation = transform.getRotation();

        ImGui.spacing();
        ImGui.text("Active Camera");
        ImGui.separator();
        ImGui.text(String.format("Position: %.2f, %.2f, %.2f", position.x, position.y, position.z));
        ImGui.text(String.format(
            "Rotation: %.2f, %.2f, %.2f",
            Math.toDegrees(rotation.x),
            Math.toDegrees(rotation.y),
            Math.toDegrees(rotation.z)
        ));
    }

    private void drawSceneObjectNode(SceneObject object) {
        boolean selected = object == selectedObject;
        String label = object.getName() + "##" + System.identityHashCode(object);

        if (object.getChildren().isEmpty()) {
            if (ImGui.selectable(label, selected)) {
                selectedObject = object;
            }

            return;
        }

        if (ImGui.treeNode(label)) {
            if (ImGui.isItemClicked()) {
                selectedObject = object;
            }

            for (SceneObject child : object.getChildren()) {
                drawSceneObjectNode(child);
            }

            ImGui.treePop();
        } else if (ImGui.isItemClicked()) {
            selectedObject = object;
        }
    }

    private void drawSelectedObjectInspector(Scene scene) {
        if (selectedObject == null)
            return;

        if (!isObjectInScene(scene, selectedObject)) {
            selectedObject = null;
            return;
        }

        var transform = selectedObject.getTransform();
        var position = transform.getPosition();
        var rotation = transform.getRotation();
        var scale = transform.getScale();

        ImGui.spacing();
        ImGui.text("Selected Object");
        ImGui.separator();
        ImGui.text("Name: " + selectedObject.getName());
        ImGui.text("Active: " + selectedObject.isActive());
        ImGui.text("Visible: " + selectedObject.isVisible());
        ImGui.text(
            "Layer Mask: " +
            selectedObject.getLayerMask() +
            " (" + RenderLayers.describe(selectedObject.getLayerMask()) + ")"
        );

        float[] positionValues = { position.x, position.y, position.z };
        if (ImGui.dragFloat3("Position", positionValues, 0.05f)) {
            transform.setPosition(positionValues[0], positionValues[1], positionValues[2]);
        }

        float[] rotationValues = {
            (float) Math.toDegrees(rotation.x),
            (float) Math.toDegrees(rotation.y),
            (float) Math.toDegrees(rotation.z)
        };
        if (ImGui.dragFloat3("Rotation", rotationValues, 0.5f)) {
            transform.setRotationDegrees(rotationValues[0], rotationValues[1], rotationValues[2]);
        }

        float[] scaleValues = { scale.x, scale.y, scale.z };
        if (ImGui.dragFloat3("Scale", scaleValues, 0.05f)) {
            transform.setScale(scaleValues[0], scaleValues[1], scaleValues[2]);
        }

        drawSelectedObjectMetadata(selectedObject);
    }

    private boolean isObjectInScene(Scene scene, SceneObject target) {
        for (SceneObject root : scene.getRootObjects()) {
            if (containsObject(root, target))
                return true;
        }

        return false;
    }

    private boolean containsObject(SceneObject current, SceneObject target) {
        if (current == target)
            return true;

        for (SceneObject child : current.getChildren()) {
            if (containsObject(child, target))
                return true;
        }

        return false;
    }

    private void drawSelectedObjectMetadata(SceneObject object) {
        ImGui.spacing();

        ImGui.text("Tags");
        ImGui.separator();

        if (object.getTags().isEmpty()) {
            ImGui.text("None");
        } else {
            for (String tag : object.getTags()) {
                ImGui.bulletText(tag);
            }
        }

        ImGui.spacing();

        ImGui.text("Components");
        ImGui.separator();

        if (object.getComponents().isEmpty()) {
            ImGui.text("None");
        } else {
            for (SceneComponent component : object.getComponents()) {
                ImGui.bulletText(component.getClass().getSimpleName());
            }
        }

        ImGui.spacing();

        ImGui.text("Rendering");
        ImGui.separator();
        ImGui.text("Renderable: " + object.isRenderable());
        ImGui.text("Casts Shadow: " + object.castsShadow());
        ImGui.text(String.format("Bounding Radius: %.2f", object.getBoundingRadius()));

        drawMaterialInspector(object);
    }

    private void drawMaterialInspector(SceneObject object) {
        ImGui.spacing();

        ImGui.text("Material");
        ImGui.separator();

        if (!object.isRenderable()) {
            ImGui.text("None");
            return;
        }

        var material = object.getMaterial();

        ImGui.text("Material ID: " + material.getId());
        ImGui.text("Shader ID: " + material.getShader().getId());

        ImGui.text(String.format(
            "Tint: %.2f, %.2f, %.2f",
            material.getTint().x,
            material.getTint().y,
            material.getTint().z
        ));

        ImFloat metallic = new ImFloat(material.getMetallic());
        if (ImGui.sliderFloat("Metallic", metallic.getData(), 0.0f, 1.0f, "%.2f")) {
            material.setMetallic(metallic.get());
        }

        ImFloat roughness = new ImFloat(material.getRoughness());
        if (ImGui.sliderFloat("Roughness", roughness.getData(), 0.0f, 1.0f, "%.2f")) {
            material.setRoughness(roughness.get());
        }

        ImFloat normalStrength = new ImFloat(material.getNormalStrength());
        if (ImGui.sliderFloat("Normal Strength", normalStrength.getData(), 0.0f, 2.0f, "%.2f")) {
            material.setNormalStrength(normalStrength.get());
        }

        ImGui.text(String.format(
            "Emissive: %.2f, %.2f, %.2f",
            material.getEmissive().x,
            material.getEmissive().y,
            material.getEmissive().z
        ));

        ImGui.spacing();

        ImGui.text("Textures");
        ImGui.separator();
        ImGui.text("Albedo: " + (material.getAlbedo() != null));
        ImGui.text("Normal: " + (material.getNormalMap() != null));
        ImGui.text("Metallic-Roughness: " + (material.getMetallicRoughnessMap() != null));
        ImGui.text("Ambient Occlusion: " + (material.getAmbientOcclusionMap() != null));
        ImGui.text("Emissive: " + (material.getEmissiveMap() != null));
    }

    @Override
    public void dispose() {
        if (!initialised)
            return;

        glBackend.shutdown();
        glfwBackend.shutdown();
        ImGui.destroyContext();

        initialised = false;
    }
}
