package com.crystal.engine.render.opengl;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;

public final class GLStateSnapshot {

    final int viewportX;
    final int viewportY;
    final int viewportWidth;
    final int viewportHeight;

    final int framebuffer;

    final boolean depthTest;
    final int depthFunc;
    final boolean depthMask;

    final boolean cullFace;
    final int cullFaceMode;
    final int frontFace;

    final boolean colorMaskR;
    final boolean colorMaskG;
    final boolean colorMaskB;
    final boolean colorMaskA;

    public GLStateSnapshot() {
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);

        this.viewportX = viewport[0];
        this.viewportY = viewport[1];
        this.viewportWidth = viewport[2];
        this.viewportHeight = viewport[3];

        this.framebuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);

        this.depthTest = glIsEnabled(GL_DEPTH_TEST);
        this.depthFunc = glGetInteger(GL_DEPTH_FUNC);
        this.depthMask = glGetBoolean(GL_DEPTH_WRITEMASK);

        this.cullFace = glIsEnabled(GL_CULL_FACE);
        this.cullFaceMode = glGetInteger(GL_CULL_FACE_MODE);
        this.frontFace = glGetInteger(GL_FRONT_FACE);

        ByteBuffer colorMask = BufferUtils.createByteBuffer(4);
        glGetBooleanv(GL_COLOR_WRITEMASK, colorMask);

        this.colorMaskR = colorMask.get(0) != 0;
        this.colorMaskG = colorMask.get(1) != 0;
        this.colorMaskB = colorMask.get(2) != 0;
        this.colorMaskA = colorMask.get(3) != 0;
    }

    public void restore() {
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

        if (depthTest) glEnable(GL_DEPTH_TEST);
        else glDisable(GL_DEPTH_TEST);

        glDepthFunc(depthFunc);
        glDepthMask(depthMask);

        if (cullFace) glEnable(GL_CULL_FACE);
        else glDisable(GL_CULL_FACE);

        glCullFace(cullFaceMode);
        glFrontFace(frontFace);

        glColorMask(colorMaskR, colorMaskG, colorMaskB, colorMaskA);
    }
}
