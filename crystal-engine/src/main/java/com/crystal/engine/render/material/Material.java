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
    private final RenderState renderState = new RenderState();

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

    public Material(Shader shader) {
        if (shader == null) throw new IllegalArgumentException("Shader cannot be null");

        this.shader = shader;
    }

    public int getId() {
        return id;
    }

    public Shader getShader() {
        return shader;
    }

    public RenderState getRenderState() {
        return renderState;
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

    public Material setTint(float r, float g, float b) {
        validateNonNegativeFiniteColor(r, g, b, "Tint");
        tint.set(r, g, b);
        return this;
    }

    public Material setRoughness(float roughness) {
        if (!Float.isFinite(roughness) || roughness < 0.0f || roughness > 1.0f)
            throw new IllegalArgumentException("Roughness must be finite and between 0 and 1");

        this.roughness = roughness;
        return this;
    }

    public Material setMetallic(float metallic) {
        if (!Float.isFinite(metallic) || metallic < 0.0f || metallic > 1.0f)
            throw new IllegalArgumentException("Metallic must be finite and between 0 and 1");

        this.metallic = metallic;
        return this;
    }

    public Material setEmissive(float r, float g, float b) {
        validateNonNegativeFiniteColor(r, g, b, "Emissive");
        emissive.set(r, g, b);
        return this;
    }

    public Material setUBO(UniformBuffer ubo) {
        this.ubo = ubo;
        return this;
    }

    public void bindProperties() {
        bindTextureUniforms();
        bindEnvironmentUniforms();
        bindScalarUniforms();
        bindTexturePresenceUniforms();

        if (ubo != null)
            ubo.bind();
    }

    private void bindTextureUniforms() {
        shader.setInt(ShaderUniforms.ALBEDO_TEXTURE, TextureSlots.ALBEDO);
        shader.setInt(ShaderUniforms.NORMAL_MAP, TextureSlots.NORMAL);
        shader.setInt(ShaderUniforms.METALLIC_ROUGHNESS_MAP, TextureSlots.METALLIC_ROUGHNESS);
        shader.setInt(ShaderUniforms.AMBIENT_OCCLUSION_MAP, TextureSlots.AMBIENT_OCCLUSION);
        shader.setInt(ShaderUniforms.EMISSIVE_MAP, TextureSlots.EMISSIVE);
    }

    private void bindEnvironmentUniforms() {
        shader.setInt(ShaderUniforms.IRRADIANCE_MAP, TextureSlots.IRRADIANCE);
        shader.setInt(ShaderUniforms.PREFILTER_MAP, TextureSlots.PREFILTER);
        shader.setInt(ShaderUniforms.BRDF_LUT, TextureSlots.BRDF_LUT);
    }

    private void bindScalarUniforms() {
        shader.setVec3(ShaderUniforms.MATERIAL_TINT, tint.x, tint.y, tint.z);
        shader.setFloat(ShaderUniforms.MATERIAL_ROUGHNESS, roughness);
        shader.setFloat(ShaderUniforms.MATERIAL_METALLIC, metallic);
        shader.setVec3(ShaderUniforms.MATERIAL_EMISSIVE, emissive.x, emissive.y, emissive.z);
    }

    private void bindTexturePresenceUniforms() {
        shader.setInt(ShaderUniforms.HAS_ALBEDO_TEXTURE, albedo != null ? 1 : 0);
        shader.setInt(ShaderUniforms.HAS_NORMAL_MAP, normalMap != null ? 1 : 0);
        shader.setInt(ShaderUniforms.HAS_METALLIC_ROUGHNESS_MAP, metallicRoughnessMap != null ? 1 : 0);
        shader.setInt(ShaderUniforms.HAS_AO_MAP, ambientOcclusionMap != null ? 1 : 0);
        shader.setInt(ShaderUniforms.HAS_EMISSIVE_MAP, emissiveMap != null ? 1 : 0);
    }

    private static void validateNonNegativeFiniteColor(float r, float g, float b, String name) {
        if (!Float.isFinite(r) || !Float.isFinite(g) || !Float.isFinite(b)) {
            throw new IllegalArgumentException(name + " color channels must be finite");
        }

        if (r < 0.0f || g < 0.0f || b < 0.0f) {
            throw new IllegalArgumentException(name + " color channels cannot be negative");
        }
    }
}
