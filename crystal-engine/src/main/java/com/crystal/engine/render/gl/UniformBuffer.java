package com.crystal.engine.render.gl;

import com.crystal.engine.graphics.BufferUsage;

import static org.lwjgl.opengl.GL46.*;

public class UniformBuffer extends GLObject {

    private final int bindingPoint;

    public UniformBuffer(int bindingPoint, int sizeBytes, BufferUsage usage) {
        super(glCreateBuffers());

        this.bindingPoint = bindingPoint;
        glNamedBufferData(id, sizeBytes, usage.glValue);
    }

    public UniformBuffer(int bindingPoint, int sizeBytes) {
        this(bindingPoint, sizeBytes, BufferUsage.DYNAMIC);
    }

    public void setData(long offset, float[] data) {
        glNamedBufferSubData(id, offset, data);
    }

    public void bind() {
        glBindBufferBase(GL_UNIFORM_BUFFER, bindingPoint, id);
    }

    @Override
    public void disposeInternal() {
        glDeleteBuffers(id);
    }
}
