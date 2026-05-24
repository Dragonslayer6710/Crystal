package com.crystal.engine.render;

import com.crystal.engine.core.ResourceManager;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.material.RenderState;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.shader.ShaderUniforms;
import com.crystal.engine.render.shadow.ShadowMap;
import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.texture.TextureSlots;
import com.crystal.engine.render.uniform.SceneUniformData;

import java.util.Arrays;

import static org.lwjgl.opengl.GL46.*;

public class RenderContext {

    private static final int MAX_TEXTURE_UNITS = 16;

    private final RenderResources resources;

    private float aspectRatio = 1.0f;
    private int debugViewMode = 0;
    private float exposure = 1.0f;
    private boolean hasIBL;
    private float iblDiffuseIntensity = 1.0f;
    private float iblSpecularIntensity = 1.0f;
    private boolean shadowsEnabled = true;
    private float shadowStrength = 0.6f;

    private final SceneUniformData sceneUniformData = new SceneUniformData();

    private Boolean currentDepthTest;
    private Boolean currentCullFace;
    private Boolean currentWireframe;
    private int currentShaderId = 0;
    private int currentMaterialId = 0;
    private int currentMeshId = 0;
    private final int[] boundTextures = new int[MAX_TEXTURE_UNITS];

    RenderContext(ResourceManager resourceManager, ShadowMap directionalShadowMap) {
        if (resourceManager == null)
            throw new IllegalArgumentException("ResourceManager cannot be null");

        if (directionalShadowMap == null)
            throw new IllegalArgumentException("Directional shadow map cannot be null");

        this.resources = new RenderResources(
                resourceManager.getDefaultWhiteTexture(),
                resourceManager.getDefaultNormalTexture(),
                resourceManager.getDefaultBlackCubemap(),
                resourceManager.getDefaultBrdfLut(),
                resourceManager.getSkyboxShader(),
                resourceManager.getSkyboxCubeMesh(),
                resourceManager.createEngineShaderProgram("shadow_depth"),
                directionalShadowMap
        );
    }

    void beginFrame() {
        resetBindingCache();
    }

    void prepareScene(Scene scene, float aspectRatio) {
        this.aspectRatio = aspectRatio;

        var environment = scene.getEnvironment();

        this.hasIBL = environment.hasIBL();

        float[] data = sceneUniformData.from(scene, aspectRatio);

        var sceneUBO = scene.getSceneUBO();
        sceneUBO.setData(0, data);
        sceneUBO.bind();

        bindTextureIfNeeded(
                environment.getIrradianceMap() != null ? environment.getIrradianceMap() :
                        resources.getDefaultBlackCubemap(),
                TextureSlots.IRRADIANCE
        );

        bindTextureIfNeeded(
                environment.getPrefilterMap() != null ? environment.getPrefilterMap() :
                        resources.getDefaultBlackCubemap(),
                TextureSlots.PREFILTER
        );

        bindTextureIfNeeded(
                environment.getBrdfLut() != null ? environment.getBrdfLut() :
                        resources.getDefaultBrdfLut(),
                TextureSlots.BRDF_LUT
        );

        shadowStrength = scene.getDirectionalLight().getShadowStrength();

        iblDiffuseIntensity = environment.getIblDiffuseIntensity();
        iblSpecularIntensity = environment.getIblSpecularIntensity();
    }

    void resetStateCache() {
        currentDepthTest = null;
        currentCullFace = null;
        currentWireframe = null;

        resetBindingCache();
    }

    public void applyRenderState(RenderState state) {
        if (currentDepthTest == null || state.isDepthTest() != currentDepthTest) {
            currentDepthTest = state.isDepthTest();

            if (currentDepthTest) {
                glEnable(GL_DEPTH_TEST);
            } else {
                glDisable(GL_DEPTH_TEST);
            }
        }

        if (currentCullFace == null || state.isCullFace() != currentCullFace) {
            currentCullFace = state.isCullFace();

            if (currentCullFace) {
                glEnable(GL_CULL_FACE);
            } else {
                glDisable(GL_CULL_FACE);
            }
        }

        if (currentWireframe == null || state.isWireframe() != currentWireframe) {
            currentWireframe = state.isWireframe();

            glPolygonMode(
                    GL_FRONT_AND_BACK,
                    currentWireframe ? GL_LINE : GL_FILL
            );
        }
    }

    public void bindMaterial(Material material) {
        Shader shader = material.getShader();
        int shaderId = shader.getId();

        if (shaderId != currentShaderId) {
            shader.bind();
            currentShaderId = shaderId;
        }

        bindFrameUniforms(shader);

        if (material.getId() != currentMaterialId) {
            material.bindProperties();
            currentMaterialId = material.getId();
        }

        bindMaterialTextures(material);
    }

    public void bindMesh(Mesh mesh) {
        if (mesh.getId() != currentMeshId) {
            mesh.bind();
            currentMeshId = mesh.getId();
        }
    }

    void setExposure(float exposure) {
        this.exposure = exposure;
    }

    void setDebugViewMode(int debugViewMode) {
        this.debugViewMode = debugViewMode;
    }

    void setShadowsEnabled(boolean shadowsEnabled) {
        this.shadowsEnabled = shadowsEnabled;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    RenderResources getResources() {
        return resources;
    }

    private void bindFrameUniforms(Shader shader) {
        shader.setInt(ShaderUniforms.DEBUG_VIEW_MODE, debugViewMode);
        shader.setFloat(ShaderUniforms.EXPOSURE, exposure);
        shader.setInt(ShaderUniforms.HAS_IBL, hasIBL ? 1 : 0);

        shader.setFloat(ShaderUniforms.IBL_DIFFUSE_INTENSITY, iblDiffuseIntensity);
        shader.setFloat(ShaderUniforms.IBL_SPECULAR_INTENSITY, iblSpecularIntensity);

        shader.setInt(ShaderUniforms.SHADOW_MAP, TextureSlots.SHADOW_MAP);
        shader.setInt(ShaderUniforms.HAS_SHADOWS, shadowsEnabled ? 1 : 0);
        shader.setFloat(ShaderUniforms.SHADOW_STRENGTH, shadowStrength);
    }

    private void bindMaterialTextures(Material material) {
        Texture albedo = material.getAlbedo() != null
                ? material.getAlbedo()
                : resources.getDefaultWhiteTexture();

        Texture normalMap = material.getNormalMap() != null
                ? material.getNormalMap()
                : resources.getDefaultNormalTexture();

        Texture metallicRoughness = material.getMetallicRoughnessMap() != null
                ? material.getMetallicRoughnessMap()
                : resources.getDefaultWhiteTexture();

        Texture ambientOcclusion = material.getAmbientOcclusionMap() != null
                ? material.getAmbientOcclusionMap()
                : resources.getDefaultWhiteTexture();

        Texture emissiveMap = material.getEmissiveMap() != null
                ? material.getEmissiveMap()
                : resources.getDefaultWhiteTexture();

        bindTextureIfNeeded(albedo, TextureSlots.ALBEDO);
        bindTextureIfNeeded(normalMap, TextureSlots.NORMAL);
        bindTextureIfNeeded(metallicRoughness, TextureSlots.METALLIC_ROUGHNESS);
        bindTextureIfNeeded(ambientOcclusion, TextureSlots.AMBIENT_OCCLUSION);
        bindTextureIfNeeded(emissiveMap, TextureSlots.EMISSIVE);

        bindTextureIfNeeded(resources.getDirectionalShadowMap().getDepthTexture(), TextureSlots.SHADOW_MAP);
    }

    private void bindTextureIfNeeded(Texture texture, int unit) {
        if (unit < 0 || unit >= boundTextures.length)
            throw new IllegalArgumentException("Texture unit out of range: " + unit);

        int textureId = texture != null ? texture.getId() : 0;

        if (boundTextures[unit] == textureId)
            return;

        boundTextures[unit] = textureId;

        if (texture != null) {
            texture.bind(unit);
        } else {
            glBindTextureUnit(unit, 0);
        }
    }

    private void resetBindingCache() {
        currentShaderId = 0;
        currentMaterialId = 0;
        currentMeshId = 0;

        Arrays.fill(boundTextures, 0);
    }
}
