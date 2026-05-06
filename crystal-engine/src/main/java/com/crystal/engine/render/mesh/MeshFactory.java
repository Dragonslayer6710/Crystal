package com.crystal.engine.render.mesh;

import com.crystal.engine.core.ResourceManager;
import com.crystal.engine.render.api.PrimitiveType;

public final class MeshFactory {
    private static final float[] CUBE_VERTICES = {
            // FRONT (+Z)
            // position              color    uv    normal
            -0.5f,  0.5f,  0.5f,   1,0,0,   0,1,  0,0,1,
            -0.5f, -0.5f,  0.5f,   1,0,0,   0,0,  0,0,1,
             0.5f, -0.5f,  0.5f,   1,0,0,   1,0,  0,0,1,
             0.5f,  0.5f,  0.5f,   1,0,0,   1,1,  0,0,1,

            // RIGHT (+X)
             0.5f,  0.5f,  0.5f,   0,1,0,   0,1,  1,0,0,
             0.5f, -0.5f,  0.5f,   0,1,0,   0,0,  1,0,0,
             0.5f, -0.5f, -0.5f,   0,1,0,   1,0,  1,0,0,
             0.5f,  0.5f, -0.5f,   0,1,0,   1,1,  1,0,0,

            // BACK (-Z)
             0.5f,  0.5f, -0.5f,   0,0,1,   0,1,  0,0,-1,
             0.5f, -0.5f, -0.5f,   0,0,1,   0,0,  0,0,-1,
            -0.5f, -0.5f, -0.5f,   0,0,1,   1,0,  0,0,-1,
            -0.5f,  0.5f, -0.5f,   0,0,1,   1,1,  0,0,-1,

            // LEFT (-X)
            -0.5f,  0.5f, -0.5f,   1,1,0,   0,1,  -1,0,0,
            -0.5f, -0.5f, -0.5f,   1,1,0,   0,0,  -1,0,0,
            -0.5f, -0.5f,  0.5f,   1,1,0,   1,0,  -1,0,0,
            -0.5f,  0.5f,  0.5f,   1,1,0,   1,1,  -1,0,0,

            // TOP (+Y)
            -0.5f,  0.5f, -0.5f,   1,0,1,   0,1,  0,1,0,
            -0.5f,  0.5f,  0.5f,   1,0,1,   0,0,  0,1,0,
             0.5f,  0.5f,  0.5f,   1,0,1,   1,0,  0,1,0,
             0.5f,  0.5f, -0.5f,   1,0,1,   1,1,  0,1,0,

            // BOTTOM (-Y)
            -0.5f, -0.5f,  0.5f,   0,1,1,   0,1,  0,-1,0,
            -0.5f, -0.5f, -0.5f,   0,1,1,   0,0,  0,-1,0,
             0.5f, -0.5f, -0.5f,   0,1,1,   1,0,  0,-1,0,
             0.5f, -0.5f,  0.5f,   0,1,1,   1,1,  0,-1,0,
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
        return resources.createMesh(
                PrimitiveType.TRIANGLES,
                CUBE_VERTICES,
                CUBE_INDICES,
                VertexLayout.POSITION_COLOR_UV_NORMAL
        );
    }
}
