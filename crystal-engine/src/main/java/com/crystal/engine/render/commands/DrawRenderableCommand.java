package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderCommand;
import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.Renderable;

import static org.lwjgl.opengl.GL46.*;

public class DrawRenderableCommand implements RenderCommand {

    private final Renderable renderable;
    private final Camera camera;
    private final float aspectRatio;

    public DrawRenderableCommand(Renderable renderable, Camera camera, float aspectRatio) {
        this.renderable = renderable;
        this.camera = camera;
        this.aspectRatio = aspectRatio;
    }

    @Override
    public void execute() {
        var material = renderable.getMaterial();
        var mesh = renderable.getMesh();
        var transform = renderable.getTransform();

        material.bind();
        mesh.bind();

        material.getShaderProgram().setMat4(
                "model",
                transform.getModelMatrix()
        );

        material.getShaderProgram().setMat4(
                "view",
                camera.getViewMatrix()
        );

        material.getShaderProgram().setMat4(
                "projection",
                camera.getProjectionMatrix(aspectRatio)
        );

        glDrawArrays(
                mesh.getPrimTypeValue(),
                0,
                mesh.getVertexCount()
        );

        mesh.unbind();
    }
}
