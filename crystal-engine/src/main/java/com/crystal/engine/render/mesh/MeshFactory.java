package com.crystal.engine.render.mesh;

import com.crystal.engine.core.ResourceManager;
import com.crystal.engine.render.mesh.primitive.MeshPrimitives;

public final class MeshFactory {

    private MeshFactory() {
    }

    public static Mesh createLitTexturedCube(ResourceManager resources) {
        return createMesh(resources, MeshPrimitives.cubePositionUvNormalTangent());
    }

    public static Mesh createPositionOnlyCube(ResourceManager resources) {
        return createMesh(resources, MeshPrimitives.cubePosition());
    }

    public static Mesh createFullscreenQuad(ResourceManager resources) {
        return createMesh(resources, MeshPrimitives.fullscreenQuadPositionUv());
    }

    public static Mesh createMesh(ResourceManager resources, MeshData data) {
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");
        if (data == null) throw new IllegalArgumentException("MeshData cannot be null");

        return resources.createMesh(data);
    }
}
