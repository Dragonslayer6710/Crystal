package com.crystal.engine.assets.model;

import com.crystal.engine.render.shader.Shader;

public final class ModelLoadOptions {

    private Shader shader;

    private boolean flipUVs = false;

    public Shader getShader() {
        return shader;
    }

    public boolean isFlipUVs() {
        return flipUVs;
    }

    public ModelLoadOptions setShader(Shader shader) {
        this.shader = shader;
        return this;
    }

    public ModelLoadOptions setFlipUVs(boolean flipUVs) {
        this.flipUVs = flipUVs;
        return this;
    }
}
