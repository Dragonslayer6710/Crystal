package com.crystal.engine.assets.model;

import com.crystal.engine.render.shader.Shader;

public final class ModelLoadOptions {

    private Shader shader;

    public Shader getShader() {
        return shader;
    }

    public ModelLoadOptions setShader(Shader shader) {
        this.shader = shader;
        return this;
    }
}
