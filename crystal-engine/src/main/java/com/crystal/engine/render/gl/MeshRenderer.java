package com.crystal.engine.render.gl;

import com.crystal.engine.render.mesh.Mesh;

import static org.lwjgl.opengl.GL46.*;

public final class MeshRenderer {

    private MeshRenderer() {}

    public static void draw(Mesh mesh) {
        if (mesh == null)
            throw new IllegalArgumentException("Mesh cannot be null");

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
    }
}
