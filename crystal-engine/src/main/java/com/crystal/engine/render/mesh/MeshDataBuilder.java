package com.crystal.engine.render.mesh;

import java.util.*;

public final class MeshDataBuilder {

    private final VertexLayout layout;
    private final List<Float> vertices = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();

    private final Map<VertexKey, Integer> uniqueVertices = new LinkedHashMap<>();

    private boolean autoIndexing;

    public MeshDataBuilder(VertexLayout layout) {
        if (layout == null) throw new IllegalArgumentException("VertexLayout cannot be null");

        this.layout = layout;
    }

    public MeshDataBuilder setAutoIndexing(boolean autoIndexing) {
        this.autoIndexing = autoIndexing;
        return this;
    }

    public int addVertex(float... vertex) {
        if (vertex == null) throw new IllegalArgumentException("Vertex cannot be null");

        if (vertex.length != layout.getFloatsPerVertex()) {
            throw new IllegalArgumentException(
                    "Expected " + layout.getFloatsPerVertex() + " floats, got " + vertex.length
            );
        }

        if (autoIndexing) {
            VertexKey key = new VertexKey(vertex);
            Integer existingIndex = uniqueVertices.get(key);

            if (existingIndex != null) {
                indices.add(existingIndex);
                return existingIndex;
            }

            int newIndex = vertexCount();
            appendVertex(vertex);
            uniqueVertices.put(key, newIndex);
            indices.add(newIndex);
            return newIndex;
        }
        int newIndex = vertexCount();
        appendVertex(vertex);
        return newIndex;
    }

    public MeshDataBuilder addIndex(int index) {
        if (index < 0) throw new IllegalArgumentException("Index cannot be negative");

        indices.add(index);
        return this;
    }

    public MeshDataBuilder addTriangle(int a, int b, int c) {
        return addIndex(a).addIndex(b).addIndex(c);
    }

    public MeshDataBuilder addQuad(int a, int b, int c, int d) {
        return addTriangle(a, b, c).addTriangle(c, d, a);
    }

    public MeshData build() {
        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++)
            vertexArray[i] = vertices.get(i);

        int[] indexArray = null;
        if (!indices.isEmpty()) {
            indexArray = new int[indices.size()];
            for (int i = 0; i < indices.size(); i++)
                indexArray[i] = indices.get(i);
        }

        return new MeshData(vertexArray, indexArray, layout);
    }

    private int vertexCount() {
        return vertices.size() / layout.getFloatsPerVertex();
    }

    private void appendVertex(float[] vertex) {
        for (float value : vertex)
            vertices.add(value);
    }

    private record VertexKey(float[] values) {
        private VertexKey {
            values = Arrays.copyOf(values, values.length);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof VertexKey other && Arrays.equals(values, other.values());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(values);
        }
    }
}
