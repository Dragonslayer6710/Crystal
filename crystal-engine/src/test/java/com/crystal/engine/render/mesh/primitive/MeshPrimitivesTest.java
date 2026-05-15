package com.crystal.engine.render.mesh.primitive;

import com.crystal.engine.render.mesh.MeshData;
import com.crystal.engine.render.mesh.VertexLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeshPrimitivesTest {

    @Test
    void cubePositionCreatesPositionOnlyCube() {
        MeshData data = MeshPrimitives.cubePosition();

        assertEquals(VertexLayout.POSITION, data.layout());
        assertEquals(8, data.vertexCount());
        assertArrayLength(36, data.indices());
    }

    @Test
    void cubePositionUvNormalTangentCreatesLitTexturedCubeData() {
        MeshData data = MeshPrimitives.cubePositionUvNormalTangent();

        assertEquals(VertexLayout.POSITION_UV_NORMAL_TANGENT, data.layout());
        assertEquals(24, data.vertexCount());
        assertArrayLength(36, data.indices());
    }

    @Test
    void fullscreenQuadCreatesPositionUvData() {
        MeshData data = MeshPrimitives.fullscreenQuadPositionUv();

        assertEquals(VertexLayout.POSITION_UV, data.layout());
        assertEquals(4, data.vertexCount());
        assertArrayLength(6, data.indices());
    }

    private static void assertArrayLength(int expectedLength, int[] values) {
        assertNotNull(values);
        assertEquals(expectedLength, values.length);
    }
}
