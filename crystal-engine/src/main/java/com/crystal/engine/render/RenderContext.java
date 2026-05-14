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
    private float iblIntensity = 1.0f;

    private float shadowStrength = 0.6f;

    private boolean currentDepthTest = true;
    private boolean currentCullFace = true;
    private boolean currentWireframe = false;

    private int currentShaderId = 0;
    private final int[] boundTextures = new int[MAX_TEXTURE_UNITS];
    private int currentMaterialId = 0;

    private int currentMeshId = 0;

    private final SceneUniformData sceneUniformData = new SceneUniformData();

    public RenderContext(ResourceManager resourceManager) {
        if (resourceManager == null)
            throw new IllegalArgumentException("ResourceManager cannot be null");

        this.resources = new RenderResources(
                resourceManager.getDefaultWhiteTexture(),
                resourceManager.getDefaultNormalTexture(),
                resourceManager.getDefaultBlackCubemap(),
                resourceManager.getDefaultBrdfLut(),
                resourceManager.getSkyboxShader(),
                resourceManager.getSkyboxCubeMesh(),
                resourceManager.createEngineShaderProgram("shadow_depth"),
                resourceManager.register(new ShadowMap(2048))
        );
    }

    public void beginFrame() {
        currentShaderId = 0;
        Arrays.fill(boundTextures, 0);
        currentMaterialId = 0;

        currentMeshId = 0;
    }

    public void applyRenderState(RenderState state) {
        if (state.isDepthTest() != currentDepthTest) {
            currentDepthTest = state.isDepthTest();

            if (currentDepthTest) {
                glEnable(GL_DEPTH_TEST);
            } else {
                glDisable(GL_DEPTH_TEST);
            }
        }

        if (state.isCullFace() != currentCullFace) {
            currentCullFace = state.isCullFace();

            if (currentCullFace) {
                glEnable(GL_CULL_FACE);
            } else {
                glDisable(GL_CULL_FACE);
            }
        }

        if (state.isWireframe() != currentWireframe) {
            currentWireframe = state.isWireframe();

            glPolygonMode(
                    GL_FRONT_AND_BACK,
                    currentWireframe ? GL_LINE : GL_FILL
            );
        }
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

    public void bindMaterial(Material material) {
        Shader shader = material.getShader();
        int shaderId = shader.getId();

        if (shaderId != currentShaderId) {
            shader.bind();
            currentShaderId = shaderId;
        }

        shader.setInt(ShaderUniforms.DEBUG_VIEW_MODE, debugViewMode);
        shader.setFloat(ShaderUniforms.EXPOSURE, exposure);
        shader.setInt(ShaderUniforms.HAS_IBL, hasIBL ? 1 : 0);
        shader.setFloat(ShaderUniforms.IBL_INTENSITY, iblIntensity);

        shader.setInt(ShaderUniforms.SHADOW_MAP, TextureSlots.SHADOW_MAP);
        shader.setFloat(ShaderUniforms.SHADOW_STRENGTH, shadowStrength);

        shader.setInt(ShaderUniforms.HAS_SHADOWS, 1);


        if (material.getId() != currentMaterialId) {
            material.bindProperties();
            currentMaterialId = material.getId();
        }

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

    public void bindMesh(Mesh mesh) {
        if (mesh.getId() != currentMeshId) {
            mesh.bind();
            currentMeshId = mesh.getId();
        }
    }

    public void prepareScene(Scene scene, float aspectRatio) {
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

        iblIntensity = environment.getIblIntensity();
    }

    public void setExposure(float exposure) {
        this.exposure = exposure;
    }

    public void setDebugViewMode(int debugViewMode) {
        if (debugViewMode < 0 || debugViewMode > 10)
            throw new IllegalArgumentException("Debug view mode must be between 0 and 10");

        this.debugViewMode = debugViewMode;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public RenderResources getResources() {
        return resources;
    }
}
