package com.crystal.engine.render.gl;

import com.crystal.engine.graphics.BufferUsage;

import static org.lwjgl.opengl.GL46.*;

public class ElementBuffer {

    private final int id;
    private final int count;

    private boolean deleted;

    public ElementBuffer(int[] indices, BufferUsage usage) {
        id = glCreateBuffers();
        this.count = indices.length;

        glNamedBufferData(id, indices, usage.glValue);
    }

    public ElementBuffer(int[] indices) {
        this(indices, BufferUsage.STATIC);
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public void delete() {
        if (deleted) return;

        glDeleteBuffers(id);
        deleted = true;
    }
}
