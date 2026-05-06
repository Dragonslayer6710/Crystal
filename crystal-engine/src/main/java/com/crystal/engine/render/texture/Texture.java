package com.crystal.engine.render.texture;

import com.crystal.engine.core.Disposable;

import static org.lwjgl.opengl.GL46.*;

public class Texture implements Disposable {

    private final int id;

    public Texture(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
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
