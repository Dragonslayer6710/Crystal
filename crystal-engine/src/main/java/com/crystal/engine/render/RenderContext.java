package com.crystal.engine.render;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.material.RenderState;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.render.texture.Texture;

import static org.lwjgl.opengl.GL46.*;

public class RenderContext {

    private int debugViewMode = 0;

    private boolean currentDepthTest = true;
    private boolean currentCullFace = true;
    private boolean currentWireframe = false;

    private int currentShaderId = 0;
    private int currentAlbedoTextureId = 0;
    private int currentNormalMapTextureId = 0;
    private int currentMetallicRoughnessTextureId = 0;
    private int currentAmbientOcclusionTextureId = 0;
    private int currentEmissiveMapId = 0;
    private int currentMaterialId = 0;

    private int currentMeshId = 0;

    private Texture defaultWhiteTexture;
    private Texture defaultNormalTexture;

    public void beginFrame() {
        currentShaderId = 0;

        currentAlbedoTextureId = 0;
        currentNormalMapTextureId = 0;
        currentMetallicRoughnessTextureId = 0;
        currentAmbientOcclusionTextureId = 0;
        currentEmissiveMapId = 0;

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

    private void bindTextureIfNeeded(Texture texture, int textureUnit, int target, int slot) {
        int textureId = texture != null ? texture.getId() : 0;

        switch (slot) {
            case 0 -> {
                if (textureId == currentAlbedoTextureId)  return;
                currentAlbedoTextureId = textureId;
            }
            case 1 -> {
                if (textureId == currentNormalMapTextureId) return;
                currentNormalMapTextureId = textureId;
            }
            case 2 -> {
                if (textureId == currentMetallicRoughnessTextureId) return;
                currentMetallicRoughnessTextureId = textureId;
            }
            case 3 -> {
                if (textureId == currentAmbientOcclusionTextureId) return;
                currentAmbientOcclusionTextureId = textureId;
            }
            case 4 -> {
                if (textureId == currentEmissiveMapId) return;
                currentEmissiveMapId = textureId;
            }
            default -> throw new IllegalArgumentException("Unsupported texture slot: " + slot);
        }

        if (texture != null) {
            texture.bind(textureUnit, target);
        } else {
            glActiveTexture(textureUnit);
            glBindTexture(target, 0);
        }
    }

    public void bindMaterial(Material material) {
        int shaderId = material.getShaderProgram().getId();

        if (shaderId != currentShaderId) {
            material.getShaderProgram().bind();
            currentShaderId = shaderId;
        }

        if (material.getId() != currentMaterialId) {
            material.bindProperties(debugViewMode);
            currentMaterialId = material.getId();
        }

        Texture albedo = material.getAlbedo() != null
                ? material.getAlbedo()
                : defaultWhiteTexture;

        Texture normalMap = material.getNormalMap() != null
                ? material.getNormalMap()
                : defaultNormalTexture;

        Texture metallicRoughness = material.getMetallicRoughnessMap() != null
                ? material.getMetallicRoughnessMap()
                : defaultWhiteTexture;

        Texture ambientOcclusion = material.getAmbientOcclusionMap() != null
                ? material.getAmbientOcclusionMap()
                : defaultWhiteTexture;

        Texture emissiveMap = material.getEmissiveMap() != null
                ? material.getEmissiveMap()
                : defaultWhiteTexture;

        bindTextureIfNeeded(albedo, GL_TEXTURE0, GL_TEXTURE_2D, 0);
        bindTextureIfNeeded(normalMap, GL_TEXTURE1, GL_TEXTURE_2D, 1);
        bindTextureIfNeeded(metallicRoughness, GL_TEXTURE2, GL_TEXTURE_2D, 2);
        bindTextureIfNeeded(ambientOcclusion, GL_TEXTURE3, GL_TEXTURE_2D, 3);
        bindTextureIfNeeded(emissiveMap, GL_TEXTURE4, GL_TEXTURE_2D, 4);
    }

    public void bindMesh(Mesh mesh) {
        if (mesh.getId() != currentMeshId) {
            mesh.bind();
            currentMeshId = mesh.getId();
        }
    }

    public void bindScene(Scene scene, float aspectRatio) {
        float[] data = new float[48];

        var camera = scene.getCamera();

        camera.getViewMatrix().get(data, 0);
        camera.getProjectionMatrix(aspectRatio).get(data, 16);

        var ambientColor = scene.getAmbientColor();
        data[32] = ambientColor.x;
        data[33] = ambientColor.y;
        data[34] = ambientColor.z;
        data[35] = scene.getAmbientIntensity();

        var cameraPosition = camera.getTransform().getWorldPosition();
        data[36] = cameraPosition.x;
        data[37] = cameraPosition.y;
        data[38] = cameraPosition.z;
        data[39] = 0.0f;

        var lightDirection = scene.getDirectionalLight().getDirection();
        data[40] = lightDirection.x;
        data[41] = lightDirection.y;
        data[42] = lightDirection.z;
        data[43] = 0.0f;

        var lightColor = scene.getDirectionalLight().getColor();
        data[44] = lightColor.x;
        data[45] = lightColor.y;
        data[46] = lightColor.z;
        data[47] = scene.getDirectionalLight().getIntensity();

        var sceneUBO = scene.getSceneUBO();
        sceneUBO.setData(0, data);
        sceneUBO.bind();
    }

    public void setDefaultTextures(Texture white, Texture normal) {
        this.defaultWhiteTexture = white;
        this.defaultNormalTexture = normal;
    }

    public void setDebugViewMode(int debugViewMode) {
        if (debugViewMode < 0 || debugViewMode > 6)
            throw new IllegalArgumentException("Debug view mode must be between 0 and 6");

        this.debugViewMode = debugViewMode;
    }
}
