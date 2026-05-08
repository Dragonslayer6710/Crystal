package com.crystal.engine.assets.model;

import com.crystal.engine.core.ResourceManager;
import org.lwjgl.assimp.AIScene;

import java.nio.file.Path;

import static org.lwjgl.assimp.Assimp.*;

public final class AssimpModelLoader {
    private AssimpModelLoader() {}

    public static Model load(Path path, ResourceManager resources, ModelLoadOptions options) {
        if (path == null) throw new IllegalArgumentException("Path cannot be null");

        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");

        AIScene scene = aiImportFile(
                path.toString(),
                aiProcess_Triangulate |
                aiProcess_JoinIdenticalVertices |
                aiProcess_GenSmoothNormals |
                aiProcess_CalcTangentSpace |
                aiProcess_FlipUVs
        );

        if (scene == null) throw new RuntimeException("Failed to load model: " + path + "\n" + aiGetErrorString());

        try {
            Model model = new Model();

            // Next step: iterate scene.mMeshes(), convert AIMesh -> Crystal Mesh.
            // For now this confirms Assimp can load the model

            return model;
        } finally {
            aiReleaseImport(scene);
        }
    }

}
