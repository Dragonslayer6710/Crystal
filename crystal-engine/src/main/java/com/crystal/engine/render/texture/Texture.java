package com.crystal.engine.render.texture;

import com.crystal.engine.core.Disposable;

import static org.lwjgl.opengl.GL46.*;

public class Texture implements Disposable {

    private final int id;
    private final TextureTarget target;
    private final int width;
    private final int height;
    private final int mipLevels;
    private final String sourcePath;

    private boolean disposed;

    public Texture(int id, TextureTarget target, int width, int height, int mipLevels, String sourcePath) {
        if (target == null) throw new IllegalArgumentException("TextureTarget cannot be null");
        if (mipLevels <= 0) throw new IllegalArgumentException("Mip levels must be greater than 0");

        this.id = id;
        this.target = target;
        this.width = width;
        this.height = height;
        this.mipLevels = mipLevels;
        this.sourcePath = sourcePath;
    }

    public Texture(int id, TextureTarget target, int width, int height, String sourcePath) {
        this(id, target, width, height, 1, sourcePath);
    }

    public Texture(int id, int width, int height, String sourcePath) {
        this(id, TextureTarget.TEXTURE_2D, width, height, 1, sourcePath);
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMipLevels() {
        return mipLevels;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public boolean hasMipmaps() {
        return mipLevels > 1;
    }

    public void bind(int unit) {
        glBindTextureUnit(unit, id);
    }

    @Override
    public void dispose() {
        if (disposed) return;

        glDeleteTextures(id);
        disposed = true;
    }
}
