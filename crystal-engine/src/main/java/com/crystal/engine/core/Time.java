package com.crystal.engine.core;

public final class Time {

    private double deltaTime;
    private double elapsedTime;

    private int fps;
    private int frameCount;

    private long lastFpsUpdateTime;
    private int fpsCounter;

    void update(long frameStartNanos, long lastFrameNanos) {
        deltaTime = (frameStartNanos - lastFrameNanos) / 1_000_000_000.0;

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
}
