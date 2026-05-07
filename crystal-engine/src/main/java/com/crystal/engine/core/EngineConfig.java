package com.crystal.engine.core;

public final class EngineConfig {
    // Window
    private int width = 1280;
    private int height = 720;
    private String title = "Crystal Engine";
    private int targetFPS = 144;
    private boolean vSync = false;

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getTitle() { return title; }
    public int getTargetFPS() { return targetFPS; }
    public boolean isvSync() { return vSync; }

    public EngineConfig setWidth(int width) {
        this.width = width;
        return this;
    }

    public EngineConfig setHeight(int height) {
        this.height = height;
        return this;
    }

    public EngineConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    public EngineConfig setTargetFps(int targetFPS) {
        this.targetFPS = targetFPS;
        return this;
    }

    public EngineConfig setVSync(boolean vSync) {
        this.vSync = vSync;
        return this;
    }
}
