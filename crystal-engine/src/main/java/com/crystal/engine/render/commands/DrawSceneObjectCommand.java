package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderContext;
import com.crystal.engine.render.material.RenderState;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Scene;

import static org.lwjgl.opengl.GL46.*;

public class DrawSceneObjectCommand implements RenderCommand {

    private final SceneObject object;
    private final Scene scene;
    private final float aspectRatio;

    public DrawSceneObjectCommand(SceneObject object, Scene scene, float aspectRatio) {
        this.object = object;
        this.scene = scene;
        this.aspectRatio = aspectRatio;
    }

    private void applyRenderState(RenderState state) {
        if (state.isDepthTest()) {
            glEnable(GL_DEPTH_TEST);
        } else {
            glDisable(GL_DEPTH_TEST);
        }

        if (state.isCullFace()) {
            glEnable(GL_CULL_FACE);
        } else {
            glDisable(GL_CULL_FACE);
        }

        glPolygonMode(
                GL_FRONT_AND_BACK,
                state.isWireframe() ? GL_LINE : GL_FILL
        );
    }

    @Override
    public void execute(RenderContext context) {
        var material = object.getMaterial();
        var mesh = object.getMesh();
        var transform = object.getTransform();

        context.applyRenderState(material.getRenderState());
        context.bindMaterial(material);

        mesh.bind();

        var shader = material.getShaderProgram();
        var camera = scene.getCamera();

        shader.setMat4(
                "model",
                transform.getWorldMatrix()
        );
        shader.setMat4(
                "view",
                camera.getViewMatrix()
        );
        shader.setMat4(
                "projection",
                camera.getProjectionMatrix(aspectRatio)
        );

        shader.setVec3("ambientColor", 1.0f, 1.0f, 1.0f);
        shader.setFloat("ambientIntensity", 0.2f);

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

        if (mesh.isIndexed()) {
            glDrawElements(
                    mesh.getPrimTypeValue(),
                    mesh.getIndexCount(),
                    GL_UNSIGNED_INT,
                    0
            );
        } else {
            glDrawArrays(
                    mesh.getPrimTypeValue(),
                    0,
                    mesh.getVertexCount()
            );
        }

        mesh.unbind();
    }
}
