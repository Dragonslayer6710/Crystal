package com.crystal.engine.render.material;

import com.crystal.engine.render.shader.ShaderUniforms;
import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.gl.UniformBuffer;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.texture.TextureSlots;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicInteger;

public class Material {

    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private final int id = NEXT_ID.incrementAndGet();

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

    public Material(Shader shader) {
        if (shader == null) throw new IllegalArgumentException("Shader cannot be null");

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

    public Material setAlbedo(Texture texture) {
        albedo = texture;
        return this;
    }

    public Material setNormalMap(Texture texture) {
        normalMap = texture;
        return this;
    }

    public Material setMetallicRoughnessMap(Texture texture) {
        metallicRoughnessMap = texture;
        return this;
    }

    public Material setAmbientOcclusionMap(Texture texture) {
        ambientOcclusionMap = texture;
        return this;
    }

    public Material setEmissiveMap(Texture texture) {
        emissiveMap = texture;
        return this;
    }

    public Material setUBO(UniformBuffer ubo) {
        this.ubo = ubo;
        return this;
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
        shader.setInt(ShaderUniforms.ALBEDO_TEXTURE, TextureSlots.ALBEDO);
        shader.setInt(ShaderUniforms.NORMAL_MAP, TextureSlots.NORMAL);
        shader.setInt(ShaderUniforms.METALLIC_ROUGHNESS_MAP, TextureSlots.METALLIC_ROUGHNESS);
        shader.setInt(ShaderUniforms.AMBIENT_OCCLUSION_MAP, TextureSlots.AMBIENT_OCCLUSION);
        shader.setInt(ShaderUniforms.EMISSIVE_MAP, TextureSlots.EMISSIVE);
        
        shader.setInt(ShaderUniforms.IRRADIANCE_MAP, TextureSlots.IRRADIANCE);
        shader.setInt(ShaderUniforms.PREFILTER_MAP, TextureSlots.PREFILTER);
        shader.setInt(ShaderUniforms.BRDF_LUT, TextureSlots.BRDF_LUT);

        shader.setVec3(ShaderUniforms.MATERIAL_TINT, tint.x, tint.y, tint.z);
        shader.setFloat(ShaderUniforms.MATERIAL_ROUGHNESS, roughness);
        shader.setFloat(ShaderUniforms.MATERIAL_METALLIC, metallic);
        shader.setVec3(ShaderUniforms.MATERIAL_EMISSIVE, emissive.x, emissive.y, emissive.z);

        shader.setInt(ShaderUniforms.HAS_ALBEDO_TEXTURE, albedo != null ? 1 : 0);
        shader.setInt(ShaderUniforms.HAS_NORMAL_MAP, normalMap != null ? 1 : 0);
        shader.setInt(ShaderUniforms.HAS_METALLIC_ROUGHNESS_MAP, metallicRoughnessMap != null ? 1 : 0);
        shader.setInt(ShaderUniforms.HAS_AO_MAP, ambientOcclusionMap != null ? 1 : 0);
        shader.setInt(ShaderUniforms.HAS_EMISSIVE_MAP, emissiveMap != null ? 1 : 0);

        if (ubo != null)
            ubo.bind();
    }
}