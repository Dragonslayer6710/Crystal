package com.crystal.engine.assets.model;

import com.crystal.engine.core.ResourceManager;
import com.crystal.engine.graphics.PrimitiveType;
import com.crystal.engine.graphics.TextureSettings;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.VertexLayout;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Transform;
import com.crystal.engine.render.texture.Texture;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
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
                Material material = createMaterial(scene, aiMesh, path, resources, options);

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

    private static Material createMaterial(AIScene scene, AIMesh aiMesh, Path modelPath,
                                           ResourceManager resources, ModelLoadOptions options) {
        Material material = new Material(options.getShader());

        PointerBuffer materials = scene.mMaterials();
        if (materials == null)
            return material;

        int materialIndex = aiMesh.mMaterialIndex();
        if (materialIndex < 0 || materialIndex >= scene.mNumMaterials())
            return material;

        AIMaterial aiMaterial = AIMaterial.create(materials.get(materialIndex));

        Texture albedo = loadMaterialTexture(
                scene,
                aiMaterial,
                modelPath,
                resources,
                aiTextureType_DIFFUSE,
                TextureSettings.defaultAlbedo()
        );

        if (albedo != null)
            material.setAlbedo(albedo);

        return material;
    }

    private static Texture loadMaterialTexture(AIScene scene, AIMaterial material, Path modelPath,
                                               ResourceManager resources, int textureType, TextureSettings settings) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            AIString texturePath = AIString.calloc(stack);

            var mapping = stack.mallocInt(1);
            var uvIndex = stack.mallocInt(1);
            var blend = stack.mallocFloat(1);
            var op = stack.mallocInt(1);
            var mapMode = stack.mallocInt(3);
            var flags = stack.mallocInt(1);

            int result = aiGetMaterialTexture(
                    material,
                    textureType,
                    0,
                    texturePath,
                    mapping,
                    uvIndex,
                    blend,
                    op,
                    mapMode,
                    flags
            );

            if (result != aiReturn_SUCCESS)
                return null;

            String textureRef = texturePath.dataString();

            if (textureRef.startsWith("*")) {
                int embeddedIndex = Integer.parseInt(textureRef.substring(1));

                PointerBuffer textures = scene.mTextures();
                if (textures == null || embeddedIndex >= scene.mNumTextures())
                    return null;

                AITexture embedded = AITexture.create(textures.get(embeddedIndex));

                if (embedded.mHeight() != 0)
                    throw new UnsupportedOperationException(
                            "Ucompressed embedded Assimp textures are not supported yet");

                ByteBuffer encoded = MemoryUtil.memByteBuffer(
                        embedded.pcData().address(),
                        embedded.mWidth()
                );

                return resources.loadEmbeddedTexture(
                        modelPath.getFileName() + ":" + textureRef,
                        encoded,
                        settings
                );
            }

            Path resolved = modelPath.getParent().resolve(textureRef).normalize();
            return resources.loadTexture(resolved, settings);
        }
    }
}
