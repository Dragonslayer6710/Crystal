package com.crystal.engine.core;

import com.crystal.engine.render.RendererConfig;
import com.crystal.engine.window.WindowConfig;

public final class EngineConfig {

    private final WindowConfig windowConfig = new WindowConfig();
    private final RendererConfig rendererConfig = new RendererConfig();

    private int targetFPS = 144;
    private double maxDeltaTime = 0.25f;

    public WindowConfig getWindowConfig() { return windowConfig; }
    public RendererConfig getRendererConfig() { return rendererConfig; }

    public int getTargetFPS() { return targetFPS; }
    public long getTargetFrameTimeNanos() { return targetFPS == 0 ? 0 : 1_000_000_000L / targetFPS; }

    public double getMaxDeltaTime() { return maxDeltaTime; }

    public EngineConfig setTargetFPS(int targetFPS) {
        if (targetFPS < 0) throw new IllegalArgumentException("Target FPS must be >= 0");

        this.targetFPS = targetFPS;
        return this;
    }

    public EngineConfig setMaxDeltaTime(double maxDeltaTime) {
        if (maxDeltaTime <= 0) throw new IllegalArgumentException("Max delta time must be greater than 0");

        this.maxDeltaTime = maxDeltaTime;
        return this;
    }
}
