package com.crystal.engine.render.opengl;

import com.crystal.engine.render.buffer.BufferUsage;

import static org.lwjgl.opengl.GL46.*;

public class VertexBuffer extends GLObject{

    public VertexBuffer(float[] data, BufferUsage usage) {
        super(glCreateBuffers());

        glNamedBufferData(id, data, usage.glValue);
    }

    public VertexBuffer(float[] data) {
        this(data, BufferUsage.STATIC);
    }

    @Override
    public void disposeInternal() {
        glDeleteBuffers(id);
    }
}
