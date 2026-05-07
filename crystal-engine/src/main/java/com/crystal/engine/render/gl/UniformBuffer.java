package com.crystal.engine.render.gl;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.graphics.BufferUsage;

import static org.lwjgl.opengl.GL46.*;

public class UniformBuffer implements Disposable {

    private final int id;
    private final int bindingPoint;

    public UniformBuffer(int bindingPoint, int sizeBytes, BufferUsage usage) {
        this.bindingPoint = bindingPoint;

        id = glCreateBuffers();
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
    public void dispose() {
        glDeleteBuffers(id);
    }
}
