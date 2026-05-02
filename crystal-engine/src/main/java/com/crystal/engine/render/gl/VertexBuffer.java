package com.crystal.engine.render.gl;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL46.*;

public class VertexBuffer {
    private final int id;

    public VertexBuffer(float[] data) {
        id = glCreateBuffers();

        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip();

        glNamedBufferData(
                id,
                buffer,
                GL_STATIC_DRAW
        );

        MemoryUtil.memFree(buffer);
    }

    public int getId() {
        return id;
    }

    public void delete() {
        glDeleteBuffers(id);
    }
}
