package com.crystal.engine.render.material;

import com.crystal.engine.render.shader.ShaderUniforms;
import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.gl.UniformBuffer;
import com.crystal.engine.render.shader.Shader;
import org.joml.Vector3f;

public class Material {

    private final Shader shader;

    private Texture albedo;
    private Texture normalMap;
    private Texture metallicRoughnessMap;
    private Texture ambientOcclusionMap;
    private Texture emissiveMap;

    private UniformBuffer ubo;

    private final Vector3f tint = new Vector3f(1.0f);
    private float roughness = 0.5f;
    private float metallic = 0.0f;
    private final Vector3f emissive = new Vector3f(0.0f);

    private final RenderState renderState = new RenderState();

    private static int nextId = 1;
    private final int id = nextId++;

    public Material(Shader shader) {
        this.shader = shader;
    }

    // ---------- GETTERS ----------

    public Shader getShader() {
        return shader;
    }

    public Texture getAlbedo() {
        return albedo;
    }

    public Texture getNormalMap() {
        return normalMap;
    }

    public Texture getMetallicRoughnessMap() {
        return metallicRoughnessMap;
    }

    public Texture getAmbientOcclusionMap() {
        return ambientOcclusionMap;
    }

    public Texture getEmissiveMap() {
        return emissiveMap;
    }

    public Vector3f getTint() {
        return tint;
    }

    public float getRoughness() {
        return roughness;
    }

    public float getMetallic() {
        return metallic;
    }

    public Vector3f getEmissive() {
        return emissive;
    }

    public RenderState getRenderState() {
        return renderState;
    }

    public int getId() {
        return id;
    }


    // ---------- SETTERS (explicit, no collections) ----------

    public void setAlbedo(Texture texture) {
        albedo = texture;
    }

    public void setNormalMap(Texture texture) {
        normalMap = texture;
    }

    public void setMetallicRoughnessMap(Texture texture) {
        metallicRoughnessMap = texture;
    }

    public void setAmbientOcclusionMap(Texture texture) {
        ambientOcclusionMap = texture;
    }

    public void setEmissiveMap(Texture texture) {
        emissiveMap = texture;
    }

    public void setUBO(UniformBuffer ubo) {
        this.ubo = ubo;
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

    public Material setEmissive(float r, float g, float b) {
        emissive.set(r, g, b);
        return this;
    }

    // ---------- BIND ----------

    public void bindProperties() {
        shader.setInt(ShaderUniforms.ALBEDO_TEXTURE, 0);
        shader.setInt(ShaderUniforms.NORMAL_MAP, 1);
        shader.setInt(ShaderUniforms.METALLIC_ROUGHNESS_MAP, 2);
        shader.setInt(ShaderUniforms.AMBIENT_OCCLUSION_MAP, 3);
        shader.setInt(ShaderUniforms.EMISSIVE_MAP, 4);

        shader.setVec3(ShaderUniforms.MATERIAL_TINT, tint.x, tint.y, tint.z);
        shader.setFloat(ShaderUniforms.MATERIAL_ROUGHNESS, roughness);
        shader.setFloat(ShaderUniforms.MATERIAL_METALLIC, metallic);
        shader.setVec3(ShaderUniforms.MATERIAL_EMISSIVE, emissive.x, emissive.y, emissive.z);

        if (ubo != null)
            ubo.bind();
    }
}