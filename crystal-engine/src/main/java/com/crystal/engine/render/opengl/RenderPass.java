package com.crystal.engine.render.opengl;

import static org.lwjgl.opengl.GL46.*;

public final class RenderPass implements AutoCloseable {

    private final GLStateSnapshot snapshot;

    public RenderPass(Framebuffer framebuffer, int width, int height) {
        if (framebuffer == null) throw new IllegalArgumentException("Framebuffer cannot be null");
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("RenderPass size must be greater than 0");

        this.snapshot = new GLStateSnapshot();

        framebuffer.bind();
        glViewport(0, 0, width, height);
    }

    public RenderPass(int width, int height) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("RenderPass size must be greater than 0");

        this.snapshot = new GLStateSnapshot();

        Framebuffer.bindDefault();
        glViewport(0, 0, width, height);
    }

    public void clearColor() {
        glClear(GL_COLOR_BUFFER_BIT);
    }

    public void clearDepth() {
        glClear(GL_DEPTH_BUFFER_BIT);
    }

    public void clearColorDepth() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void close() {
        snapshot.restore();
    }
}
