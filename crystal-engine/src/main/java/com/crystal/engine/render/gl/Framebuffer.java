package com.crystal.engine.render.gl;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.render.texture.Texture;

import static org.lwjgl.opengl.GL46.*;

public class Framebuffer implements Disposable {

    private final int id;
    private final int depthRenderbuffer;

    private int width;
    private int height;
    private boolean disposed;

    public Framebuffer(String debugName) {
        this.id = glCreateFramebuffers();
        this.depthRenderbuffer = glCreateRenderbuffers();

        // Optional later: GLObjectLabel.labelFramebuffer(id, debugName);
        // Optional later: GLObjectLabel.labelRenderbuffer(depthRenderbuffer, debugName + ":depth");

        glNamedFramebufferRenderbuffer(id, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderbuffer);
    }

    public void resizeDepthBuffer(int width, int height) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Framebuffer size must be greater than 0");

        if (this.width == width && this.height == height)
            return;

        this.width = width;
        this.height = height;

        glNamedRenderbufferStorage(depthRenderbuffer, GL_DEPTH_COMPONENT24, width, height);
    }

    public void attachTexture2D(Texture texture, int mipLevel) {
        if (texture == null) throw new IllegalArgumentException("Texture cannot be null");

        glNamedFramebufferTexture(id, GL_COLOR_ATTACHMENT0, texture.getId(), mipLevel);

        glNamedFramebufferDrawBuffer(id, GL_COLOR_ATTACHMENT0);
        checkComplete();
    }

    public void attachCubemapFace(Texture texture, int mipLevel, int face) {
        if (texture == null) throw new IllegalArgumentException("Texture cannot be null");
        if (face < 0 || face >= 6) throw new IllegalArgumentException("Cubemap face must be between 0 and 5");

        glNamedFramebufferTextureLayer(id, GL_COLOR_ATTACHMENT0, texture.getId(), mipLevel, face);

        glNamedFramebufferDrawBuffer(id, GL_COLOR_ATTACHMENT0);
        checkComplete();
    }

    public void attachDepthBuffer() {
        glNamedFramebufferRenderbuffer(
                id,
                GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER,
                depthRenderbuffer
        );
    }

    public void detachDepthBuffer() {
        glNamedFramebufferRenderbuffer(
                id,
                GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER,
                0
        );
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id);
    }

    public int getId() {
        return id;
    }

    public void checkComplete() {
        int status = glCheckNamedFramebufferStatus(id, GL_FRAMEBUFFER);

        if (status != GL_FRAMEBUFFER_COMPLETE)
            throw new IllegalStateException("Framebuffer is incomplete: 0x" + Integer.toHexString(status));
    }

    @Override
    public void dispose() {
        if (disposed)
            return;

        glDeleteFramebuffers(id);
        glDeleteRenderbuffers(depthRenderbuffer);

        disposed = true;
    }
}
