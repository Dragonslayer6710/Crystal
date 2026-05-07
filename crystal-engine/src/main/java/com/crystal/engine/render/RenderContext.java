package com.crystal.engine.render;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.material.RenderState;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.render.shader.Shader;
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

    private int currentSceneShaderId = 0;

    private int currentMeshId = 0;

    public void beginFrame() {
        currentShaderId = 0;
        currentAlbedoTextureId = 0;
        currentNormalMapTextureId = 0;
        currentMaterialId = 0;

        currentSceneShaderId = 0;

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
        bindTextureIfNeeded(material.getAlbedo(), GL_TEXTURE1, GL_TEXTURE_2D, false);
    }

    public void bindMesh(Mesh mesh) {
        if (mesh.getId() != currentMeshId) {
            mesh.bind();
            currentMeshId = mesh.getId();
        }
    }

    public void bindScene(Shader shader, Scene scene, float aspectRatio) {
        int shaderId = shader.getId();

        if (currentSceneShaderId == shaderId)
            return;

        var camera = scene.getCamera();
        shader.setMat4(
                "view",
                camera.getViewMatrix()
        );
        shader.setMat4(
                "projection",
                camera.getProjectionMatrix(aspectRatio)
        );

        var ambientColor = scene.getAmbientColor();
        shader.setVec3(
                "ambientColour",
                ambientColor.x,
                ambientColor.y,
                ambientColor.z
        );
        shader.setFloat("ambientIntensity", scene.getAmbientIntensity());

        var light = scene.getDirectionalLight();
        shader.setVec3(
                "sun.direction",
                light.getDirection().x,
                light.getDirection().y,
                light.getDirection().z
        );
        shader.setVec3(
                "sun.color",
                light.getColor().x,
                light.getColor().y,
                light.getColor().z
        );
        shader.setFloat("sun.intensity", light.getIntensity());

        currentSceneShaderId = shaderId;
    }
}
