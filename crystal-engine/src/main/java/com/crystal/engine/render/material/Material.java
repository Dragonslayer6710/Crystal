package com.crystal.engine.render.material;

import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.gl.UniformBuffer;
import com.crystal.engine.render.shader.ShaderProgram;

import static org.lwjgl.opengl.GL46.*;

public class Material {

    private final ShaderProgram shaderProgram;

    private Texture albedo;
    private Texture normalMap;

    private UniformBuffer materialUBO;

    public Material(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    // ---------- SETTERS (explicit, no collections) ----------

    public void setAlbedo(Texture texture) {
        this.albedo = texture;
    }

    public void setNormalMap(Texture texture) {
        this.normalMap = texture;
    }

    public void setMaterialUBO(UniformBuffer ubo) {
        this.materialUBO = ubo;
    }

    // ---------- BIND ----------

    public void bind() {
        shaderProgram.bind();
        shaderProgram.setInt("albedoTexture", 0);

        if (albedo != null) {
            albedo.bind(GL_TEXTURE0, GL_TEXTURE_2D);
        }

        if (normalMap != null) {
            normalMap.bind(GL_TEXTURE1, GL_TEXTURE_2D);
        }

        if (materialUBO != null) {
             materialUBO.bind();
        }
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }
}