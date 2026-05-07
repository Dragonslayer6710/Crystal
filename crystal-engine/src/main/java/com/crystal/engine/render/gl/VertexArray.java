package com.crystal.engine.render.gl;

import static org.lwjgl.opengl.GL46.*;

public class VertexArray {
    private final int id;

    private boolean deleted;

    public VertexArray() {
        id = glCreateVertexArrays();
    }

    public void bindVertexBuffer(int bindingIndex, VertexBuffer vbo, long offset, int stride) {
        glVertexArrayVertexBuffer(
                id,
                bindingIndex,
                vbo.getId(),
                offset,
                stride
        );
    }

    public void setElementBuffer(ElementBuffer ebo) {
        glVertexArrayElementBuffer(id, ebo.getId());
    }

    public void setAttribute(int attribIndex, int bindingIndex, int size,
                             int type, boolean normalized, int relativeOffset) {
        glEnableVertexArrayAttrib(id, attribIndex);
        glVertexArrayAttribFormat(
                id,
                attribIndex,
                size,
                type,
                normalized,
                relativeOffset
        );

        glVertexArrayAttribBinding(
                id,
                attribIndex,
                bindingIndex
        );
    }

    public void bind() {
        glBindVertexArray(id);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    public void delete() {
        if (deleted) return;

        glDeleteVertexArrays(id);
        deleted = true;
    }
}
