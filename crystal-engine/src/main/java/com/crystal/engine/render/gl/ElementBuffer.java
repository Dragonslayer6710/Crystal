package com.crystal.engine.render.gl;

import static org.lwjgl.opengl.GL46.*;

public class ElementBuffer {

    private final int id;
    private final int count;

    public ElementBuffer(int[] indices) {
        id = glCreateBuffers();
        this.count = indices.length;

        glNamedBufferData(
                id,
                indices,
                GL_STATIC_DRAW
        );
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public void delete() {
        glDeleteBuffers(id);
    }
}
