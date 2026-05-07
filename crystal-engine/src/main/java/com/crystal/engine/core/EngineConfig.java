package com.crystal.engine.core;

import com.crystal.engine.window.WindowConfig;

public final class EngineConfig {

    private final WindowConfig windowConfig = new WindowConfig();

    private int targetFPS = 144;

    public WindowConfig getWindowConfig() { return windowConfig; }
    public int getTargetFPS() { return targetFPS; }

    public EngineConfig setTargetFPS(int targetFPS) {
        if (targetFPS < 0) {
            throw new IllegalArgumentException("Target FPS must be >= 0");
        }

        this.targetFPS = targetFPS;
        return this;
    }

    public long getTargetFrameTimeNanos() {
        return targetFPS == 0 ? 0 : 1_000_000_000L / targetFPS;
    }
}
