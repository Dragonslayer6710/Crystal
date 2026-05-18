package com.crystal.engine.assets.model;

import com.crystal.engine.core.ResourceManager;
import com.crystal.engine.core.exception.ModelLoadException;
import com.crystal.engine.graphics.PrimitiveType;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.VertexLayout;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Transform;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.assimp.Assimp.*;

public final class AssimpModelLoader {

    private AssimpModelLoader() {
    }

    public static Model load(Path path, ResourceManager resources, ModelLoadOptions options) {
        if (path == null) throw new IllegalArgumentException("Path cannot be null");
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");
        if (options == null) throw new IllegalArgumentException("ModelLoadOptions cannot be null");
        if (options.getShader() == null) throw new IllegalArgumentException("ModelLoadOptions shader cannot be null");

        int flags =
                aiProcess_Triangulate |
                aiProcess_JoinIdenticalVertices |
                aiProcess_GenSmoothNormals |
                aiProcess_CalcTangentSpace;

        if (options.isFlipUVs())
            flags |= aiProcess_FlipUVs;

        AIScene scene = aiImportFile(path.toString(), flags);

        if (scene == null) {
            throw new ModelLoadException("Failed to load model: " + path + "\n" + aiGetErrorString());
        }

        try {
            Model model = new Model();

            PointerBuffer meshes = scene.mMeshes();
            if (meshes == null)
                return model;

            Mesh[] loadedMeshes = new Mesh[scene.mNumMeshes()];
            Material[] loadedMaterials = new Material[scene.mNumMeshes()];

            for (int i = 0; i < scene.mNumMeshes(); i++) {
                AIMesh aiMesh = AIMesh.create(meshes.get(i));

                loadedMeshes[i] = createMesh(aiMesh, resources);
                loadedMaterials[i] = AssimpMaterialLoader.createMaterial(scene, aiMesh, path, resources, options);
            }

            AINode rootNode = scene.mRootNode();

            if (rootNode != null) {
                SceneObject rootObject = processNode(
                        rootNode,
                        path,
                        loadedMeshes,
                        loadedMaterials
                );

                model.addRootObject(rootObject);
            }

            return model;
        } finally {
            aiReleaseImport(scene);
        }
    }

    private static SceneObject processNode(AINode node, Path modelPath,
                                           Mesh[] loadedMeshes, Material[] loadedMaterials) {
        String nodeName = node.mName().dataString();

        SceneObject nodeObject = new SceneObject(
                nodeName.isBlank() ? modelPath.getFileName().toString() : nodeName,
                null,
                null,
                createTransform(node.mTransformation())
        );

        IntBuffer meshIndices = node.mMeshes();

        if (meshIndices != null) {
            for (int i = 0; i < node.mNumMeshes(); i++) {
                int meshIndex = meshIndices.get(i);

                Mesh mesh = loadedMeshes[meshIndex];

                SceneObject meshObject = new SceneObject(
                        nodeObject.getName() + "_mesh_" + i,
                        mesh,
                        loadedMaterials[meshIndex],
                        new Transform()
                );

                nodeObject.addChild(meshObject);
            }
        }

        PointerBuffer children = node.mChildren();

        if (children != null) {
            for (int i = 0; i < node.mNumChildren(); i++) {
                AINode childNode = AINode.create(children.get(i));

                nodeObject.addChild(processNode(
                        childNode,
                        modelPath,
                        loadedMeshes,
                        loadedMaterials
                ));
            }
        }

        return nodeObject;
    }

    private static Transform createTransform(AIMatrix4x4 aiMatrix) {
        Matrix4f matrix = toJOMLMatrix(aiMatrix);

        Vector3f position = new Vector3f();
        Quaternionf rotation = new Quaternionf();
        Vector3f scale = new Vector3f();

        matrix.getTranslation(position);
        matrix.getUnnormalizedRotation(rotation);
        matrix.getScale(scale);

        return new Transform()
                .setPosition(position.x, position.y, position.z)
                .setRotation(rotation)
                .setScale(scale.x, scale.y, scale.z);
    }

    private static Matrix4f toJOMLMatrix(AIMatrix4x4 aiMatrix) {
        return new Matrix4f(
                aiMatrix.a1(), aiMatrix.b1(), aiMatrix.c1(), aiMatrix.d1(),
                aiMatrix.a2(), aiMatrix.b2(), aiMatrix.c2(), aiMatrix.d2(),
                aiMatrix.a3(), aiMatrix.b3(), aiMatrix.c3(), aiMatrix.d3(),
                aiMatrix.a4(), aiMatrix.b4(), aiMatrix.c4(), aiMatrix.d4()
        );
    }

    private static Mesh createMesh(AIMesh aiMesh, ResourceManager resources) {
        int vertexCount = aiMesh.mNumVertices();

        var positions = aiMesh.mVertices();
        var normals = aiMesh.mNormals();
        var tangents = aiMesh.mTangents();
        var texCoords = aiMesh.mTextureCoords(0);

        float[] vertices = new float[vertexCount * VertexLayout.POSITION_UV_NORMAL_TANGENT.getFloatsPerVertex()];

        int offset = 0;

        for (int i = 0; i < vertexCount; i++) {
            var position = positions.get(i);

            vertices[offset++] = position.x();
            vertices[offset++] = position.y();
            vertices[offset++] = position.z();

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

            if (tangents != null) {
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
                VertexLayout.POSITION_UV_NORMAL_TANGENT
        );
    }

    private static int[] extractIndices(AIMesh aiMesh) {
        int faceCount = aiMesh.mNumFaces();
        int[] indices = new int[faceCount * 3];

        int offset = 0;

        for (int i = 0; i < faceCount; i++) {
            AIFace face = aiMesh.mFaces().get(i);
            IntBuffer faceIndices = face.mIndices();

            if (face.mNumIndices() != 3) {
                throw new ModelLoadException("Expected triangulated face with 3 indices");
            }

            indices[offset++] = faceIndices.get(0);
            indices[offset++] = faceIndices.get(1);
            indices[offset++] = faceIndices.get(2);
        }

        return indices;
    }

}
