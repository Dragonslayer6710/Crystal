package com.crystal.engine.render;

public class RenderStats {

    private int visibleObjectCount;
    private int sceneDrawCount;

    public void reset() {
        visibleObjectCount = 0;
        sceneDrawCount = 0;
    }

    public int getVisibleObjectCount() {
        return visibleObjectCount;
    }

    public int getSceneDrawCount() {
        return sceneDrawCount;
    }

    public void setVisibleObjectCount(int visibleObjectCount) {
        this.visibleObjectCount = visibleObjectCount;
    }

    public void incrementDrawCommandCount() {
        sceneDrawCount++;
    }
}
