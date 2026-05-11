package com.crystal.engine.render.mesh;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.graphics.PrimitiveType;
import com.crystal.engine.render.gl.VertexArray;
import com.crystal.engine.render.gl.ElementBuffer;
import com.crystal.engine.render.gl.VertexBuffer;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL46.*;

public class Mesh implements Disposable {

    private final VertexArray vao;
    private final VertexBuffer vbo;
    private final ElementBuffer ebo;

    private final PrimitiveType type;
    private final int vertexCount;
    private final MeshBounds bounds;

    private final VertexLayout layout;

    private static int nextId = 1;
    private final int id = nextId++;

    private boolean disposed;

    public Mesh(PrimitiveType type, float[] vertices, int[] indices, VertexLayout layout) {
        if (type == null)
            throw new IllegalArgumentException("PrimitiveType cannot be null");

        if (vertices == null)
            throw new IllegalArgumentException("Vertex data cannot be null");

        if (layout == null)
            throw new IllegalArgumentException("VertexLayout cannot be null");

        if (vertices.length == 0)
            throw new IllegalArgumentException("Vertex data cannot be empty");

        if (vertices.length % layout.getFloatsPerVertex() != 0)
            throw new IllegalArgumentException("Vertex data length must be divisible by layout floats-per-vertex");

        if (indices != null && indices.length == 0)
            throw new IllegalArgumentException("Index data cannot be empty");

        layout.validate();

        this.type = type;
        this.layout = layout;
        vertexCount = vertices.length / layout.getFloatsPerVertex();
        bounds = calculateBounds(vertices, layout);

        if (indices != null) {
            for (int index : indices) {
                if (index < 0 || index >= vertexCount) {
                    throw new IllegalArgumentException(
                            "Index " + index + " is outside vertex range 0.." + (vertexCount - 1)
                    );
                }
            }
        }

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

    private static MeshBounds calculateBounds(float[] vertices, VertexLayout layout) {
        int stride = layout.getFloatsPerVertex();

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;

        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < vertices.length; i += stride) {
            float x = vertices[i];
            float y = vertices[i + 1];
            float z = vertices[i + 2];

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);

            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        Vector3f center = new Vector3f(
                (minX + maxX) * 0.5f,
                (minY + maxY) * 0.5f,
                (minZ + maxZ) * 0.5f
        );

        float radius = 0.0f;

        for (int i = 0; i < vertices.length; i += stride) {
            float dx = vertices[i] - center.x;
            float dy = vertices[i + 1] - center.y;
            float dz = vertices[i + 2] - center.z;

            radius = Math.max(radius, (float) Math.sqrt(dx * dx + dy * dy + dz * dz));
        }

        return new MeshBounds(center, radius);
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

    public MeshBounds getBounds() {
        return bounds;
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

       vao.dispose();
       vbo.dispose();

       if (ebo != null)
           ebo.dispose();

       disposed = true;
    }
}
