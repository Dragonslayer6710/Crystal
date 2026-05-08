package com.crystal.engine.assets.model;

import com.crystal.engine.core.ResourceManager;
import com.crystal.engine.graphics.PrimitiveType;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.VertexLayout;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Transform;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;

import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.assimp.Assimp.*;

public final class AssimpModelLoader {
    private AssimpModelLoader() {}

    public static Model load(Path path, ResourceManager resources, ModelLoadOptions options) {
        if (path == null) throw new IllegalArgumentException("Path cannot be null");
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");
        if (options == null) throw new IllegalArgumentException("ModelLoadOptions cannot be null");
        if (options.getShader() == null) throw new IllegalArgumentException("ModelLoadOptions shader cannot be null");

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

            PointerBuffer meshes = scene.mMeshes();
            if (meshes == null)
                return model;

            for (int i = 0; i < scene.mNumMeshes(); i++) {
                AIMesh aiMesh = AIMesh.create(meshes.get(i));

                Mesh mesh = createMesh(aiMesh, resources);
                Material material = new Material(options.getShader());

                SceneObject object = new SceneObject(
                        aiMesh.mName().dataString().isBlank()
                                ? path.getFileName() + "_mesh_" + i
                                : aiMesh.mName().dataString(),
                        mesh,
                        material,
                        new Transform()
                );

                model.addRootObject(object);
            }

            return model;
        } finally {
            aiReleaseImport(scene);
        }
    }

    private static Mesh createMesh(AIMesh aiMesh, ResourceManager resources) {
        int vertexCount = aiMesh.mNumVertices();

        var positions = aiMesh.mVertices();
        var normals = aiMesh.mNormals();
        var tangents = aiMesh.mTangents();
        var texCoords = aiMesh.mTextureCoords(0);

        float[] vertices = new float[vertexCount * VertexLayout.POSITION_COLOR_UV_NORMAL_TANGENT.getFloatsPerVertex()];

        int offset = 0;

        for (int i = 0; i < vertexCount; i++) {
            var position = positions.get(i);

            vertices[offset++] = position.x();
            vertices[offset++] = position.y();
            vertices[offset++] = position.z();

            // Vertex colour fallback: white
            vertices[offset++] = 1.0f;
            vertices[offset++] = 1.0f;
            vertices[offset++] = 1.0f;

            if (texCoords != null) {
                var uv = texCoords.get(i);
                vertices[offset++] = uv.x();
                vertices[offset++] = uv.y();
            } else {
                vertices[offset++] = 0.0f;
                vertices[offset++] = 0.0f;
            }

            if (normals != null) {
                var normal = normals.get(i);
                vertices[offset++] = normal.x();
                vertices[offset++] = normal.y();
                vertices[offset++] = normal.z();
            } else {
                vertices[offset++] = 0.0f;
                vertices[offset++] = 0.0f;
                vertices[offset++] = 1.0f;
            }

            if (normals != null) {
                var tangent = tangents.get(i);
                vertices[offset++] = tangent.x();
                vertices[offset++] = tangent.y();
                vertices[offset++] = tangent.z();
            } else {
                vertices[offset++] = 1.0f;
                vertices[offset++] = 0.0f;
                vertices[offset++] = 0.0f;
            }
        }

        int[] indices = extractIndices(aiMesh);

        return resources.createMesh(
                PrimitiveType.TRIANGLES,
                vertices,
                indices,
                VertexLayout.POSITION_COLOR_UV_NORMAL_TANGENT
        );
    }

    private static int[] extractIndices(AIMesh aiMesh) {
        int faceCount = aiMesh.mNumFaces();
        int[] indices = new int[faceCount * 3];

        int offset = 0;

        for (int i = 0; i < faceCount; i++) {
            AIFace face = aiMesh.mFaces().get(i);
            IntBuffer faceIndices = face.mIndices();

            if (face.mNumIndices() != 3) throw new IllegalStateException("Expected triangulated face with 3 indices");

            indices[offset++] = faceIndices.get(0);
            indices[offset++] = faceIndices.get(1);
            indices[offset++] = faceIndices.get(2);
        }

        return indices;
    }
}
