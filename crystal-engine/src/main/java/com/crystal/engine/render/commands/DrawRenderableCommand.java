package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderCommand;
import com.crystal.engine.render.scene.Renderable;

import static org.lwjgl.opengl.GL46.*;

public class DrawRenderableCommand implements RenderCommand {

    private final Renderable renderable;

    public DrawRenderableCommand(Renderable renderable) {
        this.renderable = renderable;
    }

    @Override
    public void execute() {
        var material = renderable.getMaterial();
        var mesh = renderable.getMesh();

        material.bind();
        mesh.bind();

        glDrawArrays(
                mesh.getPrimTypeValue(),
                0,
                mesh.getVertexCount()
        );

        mesh.unbind();
    }
}
