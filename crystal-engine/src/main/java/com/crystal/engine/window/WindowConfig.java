package com.crystal.engine.window;

public final class WindowConfig {

    private int width = 1280;
    private int height = 720;
    private String title = "Crystal Engine";

    private boolean visible = true;
    private boolean resizable = true;
    private boolean vSync = false;
    private boolean debugContext = true;

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getTitle() { return title; }

    public boolean isVisible() { return visible; }
    public boolean isResizable() { return resizable; }
    public boolean isVSync() { return vSync; }
    public boolean isDebugContext() { return debugContext; }

    public WindowConfig setWidth(int width) {
        if (width <= 0) throw new IllegalArgumentException("Width must be greater than 0");
        this.width = width;
        return this;
    }

    public WindowConfig setHeight(int height) {
        if (height <= 0) throw new IllegalArgumentException("Height must be greater than 0");
        this.height = height;
        return this;
    }

    public WindowConfig setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }

        this.title = title;
        return this;
    }

    public WindowConfig setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public WindowConfig setResizable(boolean resizable) {
        this.resizable = resizable;
        return this;
    }

    public WindowConfig setVSync(boolean vSync) {
        this.vSync = vSync;
        return this;
    }

    public WindowConfig setDebugContext(boolean debugContext) {
        this.debugContext = debugContext;
        return this;
    }
}