package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderCommand;
import com.crystal.engine.render.scene.Camera;
import com.crystal.engine.render.scene.Renderable;

import static org.lwjgl.opengl.GL46.*;

public class DrawRenderableCommand implements RenderCommand {

    private final Renderable renderable;
    private final Camera camera;

    public DrawRenderableCommand(Renderable renderable, Camera camera) {
        this.renderable = renderable;
        this.camera = camera;
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

        glDrawArrays(
                mesh.getPrimTypeValue(),
                0,
                mesh.getVertexCount()
        );

        mesh.unbind();
    }
}
