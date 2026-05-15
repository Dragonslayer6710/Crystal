package com.crystal.engine.render.mesh;

public record MeshData(float[] vertices, int[] indices, VertexLayout layout) {

    public MeshData {
        if (vertices == null) throw new IllegalArgumentException("Vertices cannot be null");
        if (layout == null) throw new IllegalArgumentException("VertexLayout cannot be null");
        if (vertices.length == 0) throw new IllegalArgumentException("Vertices cannot be empty");
        if (vertices.length % layout.getFloatsPerVertex() != 0)
            throw new IllegalArgumentException("Vertex data length must be divisible by layout floats-per-vertex");
        if (indices != null && indices.length == 0) throw new IllegalArgumentException("Indices cannot be empty");
    }

    public int vertexCount() {
        return vertices.length / layout.getFloatsPerVertex();
    }
}
