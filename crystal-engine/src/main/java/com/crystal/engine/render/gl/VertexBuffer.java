package com.crystal.engine.render.gl;

import com.crystal.engine.graphics.BufferUsage;

import static org.lwjgl.opengl.GL46.*;

public class VertexBuffer {
    private final int id;

    private boolean deleted;

    public VertexBuffer(float[] data, BufferUsage usage) {
        id = glCreateBuffers();

        glNamedBufferData(id, data, usage.glValue);
    }

    public VertexBuffer(float[] data) {
        this(data, BufferUsage.STATIC);
    }

    public int getId() {
        return id;
    }

    public void delete() {
        if (deleted) return;

        glDeleteBuffers(id);
        deleted = true;
    }
}
