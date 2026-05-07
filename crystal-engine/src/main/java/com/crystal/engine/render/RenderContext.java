package com.crystal.engine.render;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.material.RenderState;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.render.texture.Texture;

import static org.lwjgl.opengl.GL46.*;

public class RenderContext {

    private boolean currentDepthTest = true;
    private boolean currentCullFace = true;
    private boolean currentWireframe = false;

    private int currentShaderId = 0;
    private int currentAlbedoTextureId = 0;
    private int currentNormalMapTextureId = 0;
    private int currentMaterialId = 0;

    private int currentMeshId = 0;

    public void beginFrame() {
        currentShaderId = 0;
        currentAlbedoTextureId = 0;
        currentNormalMapTextureId = 0;
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

    private void bindTextureIfNeeded(Texture texture, int textureUnit, int target, boolean albedoSlot) {
        int textureId = texture != null ? texture.getId() : 0;

        if (albedoSlot) {
            if (textureId == currentAlbedoTextureId) {
                return;
            }

            currentAlbedoTextureId = textureId;
        } else {
            if (textureId == currentNormalMapTextureId) {
                return;
            }

            currentNormalMapTextureId = textureId;
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
            material.bindProperties();
            currentMaterialId = material.getId();
        }

        bindTextureIfNeeded(material.getAlbedo(), GL_TEXTURE0, GL_TEXTURE_2D, true);
        bindTextureIfNeeded(material.getNormalMap(), GL_TEXTURE1, GL_TEXTURE_2D, false);
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

        var lightDirection = scene.getDirectionalLight().getDirection();
        data[36] = lightDirection.x;
        data[37] = lightDirection.y;
        data[38] = lightDirection.z;
        data[39] = 0.0f;

        var lightColor = scene.getDirectionalLight().getColor();
        data[40] = lightColor.x;
        data[41] = lightColor.y;
        data[42] = lightColor.z;
        data[43] = scene.getDirectionalLight().getIntensity();

        var sceneUBO = scene.getSceneUBO();
        sceneUBO.setData(0, data);
        sceneUBO.bind();
    }
}
