package com.crystal.engine.render.mesh;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.graphics.PrimitiveType;
import com.crystal.engine.render.gl.VertexArray;
import com.crystal.engine.render.gl.ElementBuffer;
import com.crystal.engine.render.gl.VertexBuffer;

import static org.lwjgl.opengl.GL46.*;

public class Mesh implements Disposable {

    private final VertexArray vao;
    private final VertexBuffer vbo;
    private final ElementBuffer ebo;

    private final PrimitiveType type;
    private final int vertexCount;

    private final VertexLayout layout;

    private static int nextId = 1;
    private final int id = nextId++;

    private boolean disposed;

    public Mesh(PrimitiveType type, float[] vertices, int[] indices, VertexLayout layout) {
        this.type = type;
        this.layout = layout;
        vertexCount = vertices.length / layout.getFloatsPerVertex();

        vao = new VertexArray();
        vbo = new VertexBuffer(vertices);

        // DSA-style setup
        vao.bindVertexBuffer(0, vbo, 0, layout.getStrideBytes());
        for (VertexAttribute attribute : layout.getAttributes())
            vao.setAttribute(
                    attribute.index(),
                    0,
                    attribute.size(),
                    GL_FLOAT,
                    false,
                    attribute.offsetBytes()
            );

        if (indices != null) {
            ebo = new ElementBuffer(indices);
            vao.setElementBuffer(ebo);
        } else {
            ebo = null;
        }
    }

    public Mesh(PrimitiveType type, float[] vertices) {
        this(type, vertices, null, VertexLayout.POSITION_COLOR);
    }

    public Mesh(PrimitiveType type, float[] vertices, int[] indices) {
        this(type, vertices, indices, VertexLayout.POSITION_COLOR);
    }

    public boolean isIndexed() {
        return ebo != null;
    }

    public int getIndexCount() {
        return ebo.getCount();
    }

    public int getPrimTypeValue() {
        return type.glValue;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getId() {
        return id;
    }

    public void bind() {
        vao.bind();
    }

    public void unbind() {
        vao.unbind();
    }

    @Override
    public void dispose() {
        if (disposed) return;

       vao.delete();
       vbo.delete();

       if (ebo != null)
           ebo.delete();

       disposed = true;
    }
}
