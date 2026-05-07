package com.crystal.engine.core;

public final class Time {

    private double deltaTime;
    private double elapsedTime;

    private int fps;
    private int frameCount;

    private long lastFpsUpdateTime = System.currentTimeMillis();
    private int fpsCounter;

    private double updateTimeMs;
    private double renderTimeMs;
    private double workFrameTimeMs;
    private double frameTimeMs;

    void setUpdateTimeNanos(long nanos) {
        updateTimeMs = nanos / 1_000_000.0;
    }

    void setRenderTimeNanos(long nanos) {
        renderTimeMs = nanos / 1_000_000.0;
    }

    void setWorkFrameTimeNanos(long nanos) {
        workFrameTimeMs = nanos / 1_000_000.0;
    }
    void setFrameTimeNanos(long nanos) {
        frameTimeMs = nanos / 1_000_000.0;
    }

    void update(long frameStartNanos, long lastFrameNanos, double maxDeltaTime) {
        deltaTime = (frameStartNanos - lastFrameNanos) / 1_000_000_000.0;

        if (deltaTime > maxDeltaTime)
            deltaTime = maxDeltaTime;

        elapsedTime += deltaTime;

        frameCount++;

        fpsCounter++;

        long nowMillis = System.currentTimeMillis();

        if (nowMillis - lastFpsUpdateTime >= 1000) {
            fps = fpsCounter;
            fpsCounter = 0;
            lastFpsUpdateTime = nowMillis;
        }
    }

    public double getDeltaTime() {
        return deltaTime;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public int getFps() {
        return fps;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public double getUpdateTimeMs() {
        return updateTimeMs;
    }

    public double getRenderTimeMs() {
        return renderTimeMs;
    }

    public double getWorkFrameTimeMs() {
        return workFrameTimeMs;
    }

    public double getFrameTimeMs() {
        return frameTimeMs;
    }

}
