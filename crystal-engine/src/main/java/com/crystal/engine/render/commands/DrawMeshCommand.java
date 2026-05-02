package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderCommand;
import com.crystal.engine.render.api.PrimitiveType;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.shader.Shader;

import static org.lwjgl.opengl.GL46.*;

public class DrawMeshCommand implements RenderCommand {

    private final Mesh mesh;
    private final Shader shader;

    public DrawMeshCommand(Mesh mesh, Shader shader) {
        this.mesh = mesh;
        this.shader = shader;
    }

    @Override
    public void execute() {
        shader.bind();
        mesh.bind();
        glDrawArrays(
                mesh.getPrimTypeValue(),
                0,
                mesh.getVertexCount()
        );
        mesh.unbind();
    }
}
