package com.crystal.engine.render.opengl;

import com.crystal.engine.render.buffer.BufferUsage;

import static org.lwjgl.opengl.GL46.*;

public class ElementBuffer extends GLObject {

    private final int count;

    public ElementBuffer(int[] indices, BufferUsage usage) {
        super(glCreateBuffers());
        this.count = indices.length;

        glNamedBufferData(id, indices, usage.glValue);
    }

    public ElementBuffer(int[] indices) {
        this(indices, BufferUsage.STATIC);
    }

    public int getCount() {
        return count;
    }

    @Override
    public void disposeInternal() {
        glDeleteBuffers(id);
    }
}
