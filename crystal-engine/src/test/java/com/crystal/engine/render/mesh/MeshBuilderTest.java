package com.crystal.engine.render.mesh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeshBuilderTest {

    @Test
    void buildCreatesMeshDataWithoutIndices() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION);
        builder.addVertex(0, 0, 0);
        builder.addVertex(1, 0, 0);
        builder.addVertex(0, 1, 0);

        MeshData data = builder.build();

        assertEquals(VertexLayout.POSITION, data.layout());
        assertEquals(3, data.vertexCount());
        assertNull(data.indices());
        assertArrayEquals(new float[] {
                0, 0, 0,
                1, 0, 0,
                0, 1, 0
        }, data.vertices());
    }

    @Test
    void addTriangleCreatesIndices() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION);
        builder.addVertex(0, 0, 0);
        builder.addVertex(1, 0, 0);
        builder.addVertex(0, 1, 0);
        builder.addTriangle(0, 1, 2);

        MeshData data = builder.build();

        assertArrayEquals(new int[] {0, 1, 2}, data.indices());
    }

    @Test
    void addQuadCreatesTwoTriangles() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION);
        builder.addVertex(0, 0, 0);
        builder.addVertex(1, 0, 0);
        builder.addVertex(1, 1, 0);
        builder.addVertex(0, 1, 0);
        builder.addQuad(0, 1, 2, 3);

        MeshData data = builder.build();

        assertArrayEquals(new int[] {0, 1, 2, 2, 3, 0}, data.indices());
    }

    @Test
    void autoIndexingReusesIdenticalVertices() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION)
                .setAutoIndexing(true);
        builder.addVertex(0, 0, 0);
        builder.addVertex(1, 0, 0);
        builder.addVertex(0, 0, 0);

        MeshData data = builder.build();

        assertEquals(2, data.vertexCount());
        assertArrayEquals(new int[] {0, 1, 0}, data.indices());
    }

    @Test
    void rejectsWrongVertexSize() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION);

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.addVertex(0,0)
        );
    }

    @Test
    void autoIndexingReturnsExistingIndexForDuplicateVertex() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION)
                .setAutoIndexing(true);

        int first = builder.addVertex(0, 0, 0);
        int duplicate = builder.addVertex(0, 0, 0);

        assertEquals(first, duplicate);
    }

    @Test
    void addVertexReturnsVertexIndex() {
        MeshDataBuilder builder = new MeshDataBuilder(VertexLayout.POSITION);

        int first = builder.addVertex(0, 0, 0);
        int second = builder.addVertex(1, 0, 0);
        int third = builder.addVertex(0, 1, 0);

        assertEquals(0, first);
        assertEquals(1, second);
        assertEquals(2, third);
    }
}
