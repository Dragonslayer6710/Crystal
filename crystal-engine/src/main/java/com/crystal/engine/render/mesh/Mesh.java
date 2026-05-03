package com.crystal.engine.render.mesh;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.render.api.PrimitiveType;
import com.crystal.engine.render.gl.VertexArray;
import com.crystal.engine.render.gl.VertexBuffer;

import static org.lwjgl.opengl.GL46.*;

public class Mesh implements Disposable {

    private final VertexArray vao;
    private final VertexBuffer vbo;

    private final PrimitiveType type;
    private final int vertexCount;

    public static final int FLOATS_PER_VERTEX = 3;
    public static final int STRIDE = FLOATS_PER_VERTEX * Float.BYTES;

    public Mesh(PrimitiveType type, float[] vertices) {
        this.type = type;
        vertexCount = vertices.length / FLOATS_PER_VERTEX;

        vao = new VertexArray();
        vbo = new VertexBuffer(vertices);

        // DSA-style setup
        vao.bindVertexBuffer(0, vbo, 0, STRIDE);
        vao.setAttribute(0, 0, 3, GL_FLOAT, false, 0);
    }

    public int getPrimTypeValue() {
        return type.glValue;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void bind() {
        vao.bind();
    }

    public void unbind() {
        vao.unbind();
    }

    @Override
    public void dispose() {
       vao.delete();
       vbo.delete();
    }
}
