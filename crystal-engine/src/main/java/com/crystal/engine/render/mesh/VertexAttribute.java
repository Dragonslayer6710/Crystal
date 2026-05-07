package com.crystal.engine.render.mesh;

public record VertexAttribute(int index, int size, int offsetBytes) {
    public VertexAttribute {
        if (index < 0)
            throw new IllegalArgumentException("Vertex attribute index cannot be negative");

        if (size <= 0)
            throw new IllegalArgumentException("Vertex attribute size must be greater than 0");

        if (offsetBytes < 0)
            throw new IllegalArgumentException("Vertex attribute offset cannot be negative");
    }
}
