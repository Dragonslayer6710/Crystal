package com.crystal.engine.render;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.material.RenderState;

import static org.lwjgl.opengl.GL46.*;

public class RenderContext {

    private boolean currentDepthTest = true;
    private boolean currentCullFace = true;
    private boolean currentWireframe = false;

    private int currentShaderId = 0;
    private int currentMaterialId = 0;

    public void beginFrame() {
        currentShaderId = 0;
        currentMaterialId = 0;
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
    }
}
