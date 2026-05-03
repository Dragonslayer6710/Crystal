package com.crystal.engine.render.gl;

import com.crystal.engine.core.Disposable;

import static org.lwjgl.opengl.GL46.*;

public class UniformBuffer implements Disposable {

    private final int id;
    private final int bindingPoint;

    public UniformBuffer(int sizeBytes, int bindingPoint) {
        this.bindingPoint = bindingPoint;

        id = glCreateBuffers();
        glNamedBufferData(id, sizeBytes, GL_DYNAMIC_DRAW);
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
