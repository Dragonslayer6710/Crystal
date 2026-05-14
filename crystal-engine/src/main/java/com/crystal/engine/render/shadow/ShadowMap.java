package com.crystal.engine.render.shadow;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.render.gl.Framebuffer;
import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.texture.TextureFactory;

public final class ShadowMap implements Disposable {

    private final int size;
    private final Texture depthTexture;
    private final Framebuffer framebuffer;

    private boolean disposed;

    public ShadowMap(int size) {
        if (size <= 0) throw new IllegalArgumentException("Shadow map size must be greater than 0");

        this.size = size;
        this.depthTexture = TextureFactory.createDepthTexture2D(
                size,
                size,
                "<generated:directional-shadow-map>"
        );

        this.framebuffer = new Framebuffer("directional-shadow-map");
        this.framebuffer.attachDepthTexture(depthTexture);
    }

    public int getSize() {
        return size;
    }

    public Texture getDepthTexture() {
        return depthTexture;
    }

    public Framebuffer getFramebuffer() {
        return framebuffer;
    }

    @Override
    public void dispose() {
        if (disposed)
            return;

        framebuffer.dispose();
        depthTexture.dispose();

        disposed = true;
    }
}
