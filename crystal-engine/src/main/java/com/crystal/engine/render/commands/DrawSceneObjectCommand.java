package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderCommand;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Scene;

import static org.lwjgl.opengl.GL46.*;

public class DrawSceneObjectCommand implements RenderCommand {

    private final SceneObject sceneObject;
    private final Scene scene;
    private final float aspectRatio;

    public DrawSceneObjectCommand(SceneObject sceneObject, Scene scene, float aspectRatio) {
        this.sceneObject = sceneObject;
        this.scene = scene;
        this.aspectRatio = aspectRatio;
    }

    @Override
    public void execute() {
        var material = sceneObject.getMaterial();
        var mesh = sceneObject.getMesh();
        var transform = sceneObject.getTransform();

        material.bind();
        mesh.bind();

        var shader = material.getShaderProgram();
        var camera = scene.getCamera();

        shader.setMat4(
                "model",
                transform.getModelMatrix()
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
