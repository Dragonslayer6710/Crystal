package com.crystal.engine.render.mesh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeshDataTest {

    @Test
    void calculatesVertexCountFromLayoutStride() {
        MeshData data = new MeshData(
                new float[] {
                        0, 0, 0,
                        1, 0, 0,
                        0, 1, 0
                },
                null,
                VertexLayout.POSITION
        );

        assertEquals(3, data.vertexCount());
    }

    @Test
    void rejectsNullVertices() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MeshData(null, null, VertexLayout.POSITION)
        );
    }

    @Test
    void rejectsNullLayout() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MeshData(new float[] {0, 0, 0}, null, null)
        );
    }

    @Test
    void rejectsEmptyVertices() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MeshData(new float[] {}, null, VertexLayout.POSITION)
        );
    }

    @Test
    void rejectsVertexDataThatDoesNotMatchLayoutStride() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MeshData(new float[] {0, 0}, null, VertexLayout.POSITION)
        );
    }

    @Test
    void rejectsEmptyIndices() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MeshData(new float[] {0, 0, 0}, new int[] {}, VertexLayout.POSITION)
        );
    }
}
