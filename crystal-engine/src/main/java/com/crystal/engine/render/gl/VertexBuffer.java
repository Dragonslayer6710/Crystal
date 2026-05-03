package com.crystal.engine.render.gl;

import static org.lwjgl.opengl.GL46.*;

public class VertexBuffer {
    private final int id;

    public VertexBuffer(float[] data) {
        id = glCreateBuffers();

        glNamedBufferData(
                id,
                data,
                GL_STATIC_DRAW
        );
    }

    public int getId() {
        return id;
    }

    public void delete() {
        glDeleteBuffers(id);
    }
}
