package com.crystal.engine.render.texture;

import com.crystal.engine.core.Disposable;

import static org.lwjgl.opengl.GL46.*;

public class Texture implements Disposable {

    private final int id;
    private final int width;
    private final int height;
    private final String sourcePath;

    public Texture(int id, int width, int height, String sourcePath) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.sourcePath = sourcePath;
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

    public void bind(int texture, int target) {
        glActiveTexture(texture);
        glBindTexture(target, id);
    }

    @Override
    public void dispose() {
         glDeleteTextures(id);
    }
}
