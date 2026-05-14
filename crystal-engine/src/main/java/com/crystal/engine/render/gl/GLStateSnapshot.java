package com.crystal.engine.render.gl;

import static org.lwjgl.opengl.GL46.*;

public final class GLStateSnapshot {

    final int viewportX;
    final int viewportY;
    final int viewportWidth;
    final int viewportHeight;

    final int framebuffer;
    final boolean depthTest;
    final boolean cullFace;

    public GLStateSnapshot() {
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);

        this.viewportX = viewport[0];
        this.viewportY = viewport[1];
        this.viewportWidth = viewport[2];
        this.viewportHeight = viewport[3];

        this.framebuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        this.depthTest = glIsEnabled(GL_DEPTH_TEST);
        this.cullFace = glIsEnabled(GL_CULL_FACE);
    }

    public void restore() {
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

        if (depthTest) glEnable(GL_DEPTH_TEST);
        else glDisable(GL_DEPTH_TEST);

        if (cullFace) glEnable(GL_CULL_FACE);
        else glDisable(GL_CULL_FACE);
    }
}

