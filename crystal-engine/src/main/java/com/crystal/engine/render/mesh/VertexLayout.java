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
    ),

    POSITION_COLOR_UV_NORMAL(
            11,
            new VertexAttribute(0, 3, 0),
            new VertexAttribute(1, 3, 3 * Float.BYTES),
            new VertexAttribute(2, 2, 6 * Float.BYTES),
            new VertexAttribute(3, 3, 8 * Float.BYTES)
    ),

    POSITION_COLOR_UV_NORMAL_TANGENT(
            14,
            new VertexAttribute(0, 3, 0),
            new VertexAttribute(1, 3, 3 * Float.BYTES),
            new VertexAttribute(2, 2, 6 * Float.BYTES),
            new VertexAttribute(3, 3, 8 * Float.BYTES),
            new VertexAttribute(4, 3, 11 * Float.BYTES)
    ),

    POSITION_UV(
            5,
            new VertexAttribute(0, 3, 0),
            new VertexAttribute(1, 2, 3 * Float.BYTES)
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

    public void validate() {
        if (floatsPerVertex <= 0)
            throw new IllegalStateException("VertexLayout floatsPerVertex must be greater than 0");

        if (attributes == null || attributes.length == 0)
            throw new IllegalStateException("VertexLayout must contain at least one attribute");

        int strideBytes = getStrideBytes();

        for (VertexAttribute attribute : attributes) {
            if (attribute == null)
                throw new IllegalStateException("VertexLayout cannot contain null attributes");

            if (attribute.offsetBytes() >= strideBytes)
                throw new IllegalStateException("Vertex attribute offset must be inside the vertex stride");
        }
    }
}
