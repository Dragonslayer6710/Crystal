package com.crystal.engine.render.texture;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.graphics.TextureTarget;

import static org.lwjgl.opengl.GL46.*;

public class Texture implements Disposable {

    private final int id;
    private final TextureTarget target;
    private final int width;
    private final int height;
    private final String sourcePath;

    private boolean disposed;

    public Texture(int id, TextureTarget target, int width, int height, String sourcePath) {
        if (target == null) throw new IllegalArgumentException("TextureTarget cannot be null");

        this.id = id;
        this.target = target;
        this.width = width;
        this.height = height;
        this.sourcePath = sourcePath;
    }

    public Texture(int id, int width, int height, String sourcePath) {
        this(id, TextureTarget.TEXTURE_2D, width, height, sourcePath);
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

    public String getSourcePath() {
        return sourcePath;
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
