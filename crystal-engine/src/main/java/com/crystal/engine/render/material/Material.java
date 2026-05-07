package com.crystal.engine.render.material;

import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.gl.UniformBuffer;
import com.crystal.engine.render.shader.Shader;
import org.joml.Vector3f;

public class Material {

    private final Shader shader;

    private Texture albedo;
    private Texture normalMap;

    private UniformBuffer materialUBO;

    private final Vector3f tint = new Vector3f(1.0f);
    private float roughness = 0.5f;
    private float metallic = 0.0f;

    private final RenderState renderState = new RenderState();

    private static int nextId = 1;
    private final int id = nextId++;

    public Material(Shader shader) {
        this.shader = shader;
    }

    // ---------- GETTERS ----------

    public Vector3f getTint() {
        return tint;
    }

    public float getRoughness() {
        return roughness;
    }

    public float getMetallic() {
        return metallic;
    }

    public RenderState getRenderState() {
        return renderState;
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

    public Material setRoughness(float roughness) {
        if (roughness < 0.0f || roughness > 1.0f)
            throw new IllegalArgumentException("Roughness must be between 0 and 1");

        this.roughness = roughness;
        return this;
    }

    public Material setMetallic(float metallic) {
        if (metallic < 0.0f || metallic > 1.0f)
            throw new IllegalArgumentException("Metallic must be between 0 and 1");

        this.metallic = metallic;
        return this;
    }

    // ---------- BIND ----------

    public void bindProperties() {
        shader.setInt(Shader.Uniforms.ALBEDO_TEXTURE, 0);
        shader.setInt(Shader.Uniforms.NORMAL_MAP, 1);

        shader.setVec3(Shader.Uniforms.MATERIAL_TINT, tint.x, tint.y, tint.z);
        shader.setFloat(Shader.Uniforms.MATERIAL_ROUGHNESS, roughness);
        shader.setFloat(Shader.Uniforms.MATERIAL_METALLIC, roughness);

        if (materialUBO != null) {
             materialUBO.bind();
        }
    }

    public Shader getShaderProgram() {
        return shader;
    }

    public Texture getAlbedo() {
        return albedo;
    }

    public Texture getNormalMap() {
        return normalMap;
    }

    public int getId() {
        return id;
    }
}