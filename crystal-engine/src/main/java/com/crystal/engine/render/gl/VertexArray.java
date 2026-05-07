package com.crystal.engine.render.gl;

import static org.lwjgl.opengl.GL46.*;

public class VertexArray extends GLObject{
    public VertexArray() {
       super(glCreateVertexArrays());
    }

    public void bindVertexBuffer(int bindingIndex, VertexBuffer vbo, long offset, int stride) {
        if (vbo == null) throw new IllegalArgumentException("VertexBuffer cannot be null");
        if (bindingIndex < 0) throw new IllegalArgumentException("Binding index cannot be negative");
        if (offset < 0) throw new IllegalArgumentException("Offset cannot be negative");
        if (stride <= 0) throw new IllegalArgumentException("Stride must be greater than 0");

        glVertexArrayVertexBuffer(
                id,
                bindingIndex,
                vbo.getId(),
                offset,
                stride
        );
    }

    public void setElementBuffer(ElementBuffer ebo) {
        if (ebo == null) throw new IllegalArgumentException("ElementBuffer cannot be null");

        glVertexArrayElementBuffer(id, ebo.getId());
    }

    public void setAttribute(int attribIndex, int bindingIndex, int size,
                             int type, boolean normalized, int relativeOffset) {
        if (attribIndex < 0) throw new IllegalArgumentException("Attribute index cannot be negative");
        if (bindingIndex < 0) throw new IllegalArgumentException("Binding index cannot be negative");
        if (size <= 0) throw new IllegalArgumentException("Attribute size must be greater than 0");
        if (relativeOffset < 0) throw new IllegalArgumentException("Relative offset cannot be negative");

        glEnableVertexArrayAttrib(id, attribIndex);
        glVertexArrayAttribFormat(id, attribIndex, size, type, normalized, relativeOffset);
        glVertexArrayAttribBinding(id, attribIndex, bindingIndex);
    }

    public void bind() {
        glBindVertexArray(id);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    @Override
    public void disposeInternal() {
        glDeleteVertexArrays(id);
    }
}
