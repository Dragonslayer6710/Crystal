package com.crystal.engine.render.mesh;

import com.crystal.engine.core.ResourceManager;
import com.crystal.engine.graphics.PrimitiveType;

public final class MeshFactory {
    private static final float[] CUBE_VERTICES = {
            // FRONT (+Z)
            // position            color    uv    normal  tangent
            -0.5f,  0.5f,  0.5f,   1,0,0,   0,1,  0,0,1,  1,0,0,
            -0.5f, -0.5f,  0.5f,   1,0,0,   0,0,  0,0,1,  1,0,0,
             0.5f, -0.5f,  0.5f,   1,0,0,   1,0,  0,0,1,  1,0,0,
             0.5f,  0.5f,  0.5f,   1,0,0,   1,1,  0,0,1,  1,0,0,

            // RIGHT (+X)
             0.5f,  0.5f,  0.5f,   0,1,0,   0,1,  1,0,0,  0,0,-1,
             0.5f, -0.5f,  0.5f,   0,1,0,   0,0,  1,0,0,  0,0,-1,
             0.5f, -0.5f, -0.5f,   0,1,0,   1,0,  1,0,0,  0,0,-1,
             0.5f,  0.5f, -0.5f,   0,1,0,   1,1,  1,0,0,  0,0,-1,

            // BACK (-Z)
             0.5f,  0.5f, -0.5f,   0,0,1,   0,1,  0,0,-1,  -1,0,0,
             0.5f, -0.5f, -0.5f,   0,0,1,   0,0,  0,0,-1,  -1,0,0,
            -0.5f, -0.5f, -0.5f,   0,0,1,   1,0,  0,0,-1,  -1,0,0,
            -0.5f,  0.5f, -0.5f,   0,0,1,   1,1,  0,0,-1,  -1,0,0,

            // LEFT (-X)
            -0.5f,  0.5f, -0.5f,   1,1,0,   0,1,  -1,0,0,  0,0,1,
            -0.5f, -0.5f, -0.5f,   1,1,0,   0,0,  -1,0,0,  0,0,1,
            -0.5f, -0.5f,  0.5f,   1,1,0,   1,0,  -1,0,0,  0,0,1,
            -0.5f,  0.5f,  0.5f,   1,1,0,   1,1,  -1,0,0,  0,0,1,

            // TOP (+Y)
            -0.5f,  0.5f, -0.5f,   1,0,1,   0,1,  0,1,0,  1,0,0,
            -0.5f,  0.5f,  0.5f,   1,0,1,   0,0,  0,1,0,  1,0,0,
             0.5f,  0.5f,  0.5f,   1,0,1,   1,0,  0,1,0,  1,0,0,
             0.5f,  0.5f, -0.5f,   1,0,1,   1,1,  0,1,0,  1,0,0,

            // BOTTOM (-Y)
            -0.5f, -0.5f,  0.5f,   0,1,1,   0,1,  0,-1,0,  1,0,0,
            -0.5f, -0.5f, -0.5f,   0,1,1,   0,0,  0,-1,0,  1,0,0,
             0.5f, -0.5f, -0.5f,   0,1,1,   1,0,  0,-1,0,  1,0,0,
             0.5f, -0.5f,  0.5f,   0,1,1,   1,1,  0,-1,0,  1,0,0,
    };

    private static final int[] CUBE_INDICES = {
             0, 1, 2,  2, 3, 0,       // front
             4, 5, 6,  6, 7, 4,       // right
             8, 9,10, 10,11, 8,       // back
            12,13,14, 14,15,12,       // left
            16,17,18, 18,19,16,       // top
            20,21,22, 22,23,20        // bottom
    };


    public static Mesh createTexturedCube(ResourceManager resources) {
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");

        return resources.createMesh(
                PrimitiveType.TRIANGLES,
                CUBE_VERTICES,
                CUBE_INDICES,
                VertexLayout.POSITION_COLOR_UV_NORMAL_TANGENT
        );
    }

    public static Mesh createPositionOnlyCube(ResourceManager resources) {
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");

        float[] vertices = {
                -1, -1, -1,
                1, -1, -1,
                1,  1, -1,
                -1,  1, -1,
                -1, -1,  1,
                1, -1,  1,
                1,  1,  1,
                -1,  1,  1
        };

        int[] indices = {
                0, 1, 2, 2, 3, 0,
                4, 6, 5, 6, 4, 7,
                4, 5, 1, 1, 0, 4,
                7, 3, 2, 2, 6, 7,
                5, 6, 2, 2, 1, 5,
                4, 0, 3, 3, 7, 4
        };

        return resources.createMesh(
                PrimitiveType.TRIANGLES,
                vertices,
                indices,
                VertexLayout.POSITION
        );
    }

    public static Mesh createFullscreenQuad(ResourceManager resources) {
        if (resources == null) {
            throw new IllegalArgumentException("ResourceManager cannot be null");
        }

        float[] vertices = {
                // position          uv
                -1.0f,  1.0f, 0.0f,  0.0f, 1.0f,
                -1.0f, -1.0f, 0.0f,  0.0f, 0.0f,
                1.0f, -1.0f, 0.0f,  1.0f, 0.0f,
                1.0f,  1.0f, 0.0f,  1.0f, 1.0f
        };

        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };

        return resources.createMesh(
                PrimitiveType.TRIANGLES,
                vertices,
                indices,
                VertexLayout.POSITION_UV
        );
    }
}
