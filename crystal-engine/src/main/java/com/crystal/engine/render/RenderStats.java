package com.crystal.engine.render;

public class RenderStats {

    private int renderableObjectCount;
    private int visibleObjectCount;
    private int culledObjectCount;

    private int sceneDrawCount;
    private int skyboxDrawCount;
    private int submittedCommandCount;

    public void reset() {
        renderableObjectCount = 0;
        visibleObjectCount = 0;
        culledObjectCount = 0;

        sceneDrawCount = 0;
        skyboxDrawCount = 0;
        submittedCommandCount = 0;
    }

    public int getRenderableObjectCount() {
        return renderableObjectCount;
    }

    public int getVisibleObjectCount() {
        return visibleObjectCount;
    }

    public int getCulledObjectCount() {
        return culledObjectCount;
    }

    public int getSceneDrawCount() {
        return sceneDrawCount;
    }

    public int getSkyboxDrawCount() {
        return skyboxDrawCount;
    }

    public int getTotalDrawCount() {
        return sceneDrawCount + skyboxDrawCount;
    }

    public int getSubmittedCommandCount() {
        return submittedCommandCount;
    }

    public String summary() {
        return String.format(
                "renderable=%d, visible=%d, culled=%d, draws=%d, sceneDraws=%d, skyboxDraws=%d, submittedCommands=%d",
                renderableObjectCount,
                visibleObjectCount,
                culledObjectCount,
                getTotalDrawCount(),
                sceneDrawCount,
                skyboxDrawCount,
                submittedCommandCount
        );
    }

    void setRenderableObjectCount(int renderableObjectCount) {
        this.renderableObjectCount = renderableObjectCount;
    }

    public void setVisibleObjectCount(int visibleObjectCount) {
        this.visibleObjectCount = visibleObjectCount;
    }

    void setCulledObjectCount(int culledObjectCount) {
        this.culledObjectCount = culledObjectCount;
    }

    public void incrementSceneDrawCommandCount() {
        sceneDrawCount++;
    }

    public void incrementSkyboxDrawCommandCount() {
        skyboxDrawCount++;
    }

    public void incrementSubmittedCommandCount() {
        submittedCommandCount++;
    }
}
