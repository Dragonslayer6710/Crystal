package com.crystal.engine.render.mesh;

public enum VertexLayout {
    POSITION(
            3,
            new VertexAttribute(0, 3, 0)
    ),

    POSITION_COLOR(
            6,
            new VertexAttribute(0, 3, 0),
            new VertexAttribute(1, 3, 3 * Float.BYTES)
    ),

    POSITION_COLOR_UV(
            8,
            new VertexAttribute(0, 3, 0),
            new VertexAttribute(1, 3, 3 * Float.BYTES),
            new VertexAttribute(2, 2, 6 * Float.BYTES)
    );

    private final int floatsPerVertex;
    private final VertexAttribute[] attributes;

    VertexLayout(int floatsPerVertex, VertexAttribute... attributes) {
        this.floatsPerVertex = floatsPerVertex;
        this.attributes = attributes;
    }

    public int getFloatsPerVertex() {
        return floatsPerVertex;
    }

    public int getStrideBytes() {
        return floatsPerVertex * Float.BYTES;
    }

    public VertexAttribute[] getAttributes() {
        return attributes;
    }
}
