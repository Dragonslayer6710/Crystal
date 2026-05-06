package com.crystal.engine.render.material;

import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.gl.UniformBuffer;
import com.crystal.engine.render.shader.ShaderProgram;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL46.*;

public class Material {

    private final ShaderProgram shaderProgram;

    private Texture albedo;
    private Texture normalMap;

    private UniformBuffer materialUBO;

    private final Vector3f tint = new Vector3f(1.0f);

    private boolean wireframe = false;

    public Material(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    // ---------- GETTERS ----------

    public Vector3f getTint() {
        return tint;
    }

    public boolean isWireframe() {
        return wireframe;
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

    public Material setTint(float r, float g, float b) {
        tint.set(r, g, b);
        return this;
    }

    public Material setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
        return this;
    }

    // ---------- BIND ----------

    public void bind() {
        shaderProgram.bind();

        shaderProgram.setInt("albedoTexture", 0);
        shaderProgram.setVec3("materialTint", tint.x, tint.y, tint.z);

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