package com.crystal.engine.debug;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.core.Time;
import com.crystal.engine.render.RenderStats;
import com.crystal.engine.render.Renderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

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
        "Shadow"
    };

    private final ImGuiImplGlfw glfwBackend = new ImGuiImplGlfw();
    private final ImGuiImplGl3 glBackend = new ImGuiImplGl3();

    private boolean initialised;

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

    public void render(Time time, Renderer renderer) {
        if (!initialised)
            return;

        glfwBackend.newFrame();
        glBackend.newFrame();
        ImGui.newFrame();

        drawPerformanceWindow(time, renderer);

        ImGui.render();
        glBackend.renderDrawData(ImGui.getDrawData());
    }

    private void drawPerformanceWindow(Time time, Renderer renderer) {
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
        ImGui.text("Frustum Culling: " + renderer.isFrustumCullingEnabled());
        ImGui.text("Renderable: " + stats.getRenderableObjectCount());
        ImGui.text("Culled: " + stats.getCulledObjectCount());
        ImGui.text("Draws: " + stats.getTotalDrawCount());
        ImGui.text("Submitted Commands: " + stats.getSubmittedCommandCount());

        ImGui.end();
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

    public boolean wantsMouse() {
        return initialised && ImGui.getIO().getWantCaptureMouse();
    }

    public boolean wantsKeyboard() {
        return initialised && ImGui.getIO().getWantCaptureKeyboard();
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
