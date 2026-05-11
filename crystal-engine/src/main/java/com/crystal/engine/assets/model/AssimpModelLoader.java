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
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.assimp.Assimp.*;

public final class AssimpModelLoader {

    private static final Logger logger = LoggerFactory.getLogger(AssimpModelLoader.class);

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

        if (scene == null) throw new RuntimeException("Failed to load model: " + path + "\n" + aiGetErrorString());

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
                loadedMaterials[i] = createMaterial(scene, aiMesh, path, resources, options);
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
        Material material = new Material(options.getShader())
                .setRoughness(1.0f)
                .setMetallic(0.0f);

        PointerBuffer materials = scene.mMaterials();
        if (materials == null)
            return material;

        int materialIndex = aiMesh.mMaterialIndex();
        if (materialIndex < 0 || materialIndex >= scene.mNumMaterials())
            return material;

        AIMaterial aiMaterial = AIMaterial.create(materials.get(materialIndex));

        String materialName = getMaterialName(aiMaterial);

        loadAlbedo(scene, aiMaterial, modelPath, resources, material);
        loadNormal(scene, aiMaterial, modelPath, resources, material);
        loadMetallicRoughness(scene, aiMaterial, modelPath, resources, material);
        loadAmbientOcclusion(scene, aiMaterial, modelPath, resources, material);
        loadEmissive(scene, aiMaterial, modelPath, resources, material);

        logger.info(
                "Loaded material '{}' for mesh '{}': albedo={}, normal={}, metallicRoughness={}, ao={}, emissive={}",
                materialName,
                aiMesh.mName().dataString(),
                material.getAlbedo() != null,
                material.getNormalMap() != null,
                material.getMetallicRoughnessMap() != null,
                material.getAmbientOcclusionMap() != null,
                material.getEmissiveMap() != null
        );

        return material;
    }

    private static String getMaterialName(AIMaterial material) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            AIString name = AIString.calloc(stack);

            int result = aiGetMaterialString(
                    material,
                    AI_MATKEY_NAME,
                    aiTextureType_NONE,
                    0,
                    name
            );

            if (result != aiReturn_SUCCESS || name.dataString().isBlank())
                return "<unnamed>";

            return name.dataString();
        }
    }

    private static void loadAlbedo(AIScene scene, AIMaterial aiMaterial, Path modelPath,
                                   ResourceManager resources, Material material) {
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
    }

    private static void loadNormal(AIScene scene, AIMaterial aiMaterial, Path modelPath,
                                   ResourceManager resources, Material material) {
        Texture normal = loadMaterialTexture(
                scene,
                aiMaterial,
                modelPath,
                resources,
                aiTextureType_NORMALS,
                TextureSettings.defaultData()
        );

        // Some Assets store normal maps as aiTextureType_HEIGHT so also fallback:
        if (normal == null) {
            normal = loadMaterialTexture(
                    scene,
                    aiMaterial,
                    modelPath,
                    resources,
                    aiTextureType_HEIGHT,
                    TextureSettings.defaultData()
            );
        }

        if (normal != null)
            material.setNormalMap(normal);
    }

    private static void loadMetallicRoughness(AIScene scene, AIMaterial aiMaterial, Path modelPath,
                                   ResourceManager resources, Material material) {

        Texture metallicRoughness = loadMaterialTexture(
                scene,
                aiMaterial,
                modelPath,
                resources,
                aiTextureType_METALNESS,
                TextureSettings.defaultData()
        );

        // Some Assets store Metallic Roughness maps as aiTextureType_DIFFUSE_ROUGHNESS so also fallback:
        if (metallicRoughness == null) {
            metallicRoughness = loadMaterialTexture(
                    scene,
                    aiMaterial,
                    modelPath,
                    resources,
                    aiTextureType_DIFFUSE_ROUGHNESS,
                    TextureSettings.defaultData()
            );
        }

        if (metallicRoughness != null) {
            material.setMetallicRoughnessMap(metallicRoughness);

            // glTF metallicFactor defaults to 1.0 when a metallic-roughness map is present
            material.setMetallic(1.0f);
            material.setRoughness(1.0f);
        }
    }

    private static void loadAmbientOcclusion(AIScene scene, AIMaterial aiMaterial, Path modelPath,
                                              ResourceManager resources, Material material) {
        Texture ambientOcclusion = loadMaterialTexture(
                scene,
                aiMaterial,
                modelPath,
                resources,
                aiTextureType_AMBIENT_OCCLUSION,
                TextureSettings.defaultData()
        );

        // Some Assets store Ambient Occlusion maps as aiTextureType_LIGHTMAP so also fallback:
        if (ambientOcclusion == null) {
            ambientOcclusion = loadMaterialTexture(
                    scene,
                    aiMaterial,
                    modelPath,
                    resources,
                    aiTextureType_LIGHTMAP,
                    TextureSettings.defaultData()
            );
        }

        if (ambientOcclusion != null)
            material.setAmbientOcclusionMap(ambientOcclusion);
    }

    private static void loadEmissive(AIScene scene, AIMaterial aiMaterial, Path modelPath,
                                             ResourceManager resources, Material material) {
        Texture emissiveMap = loadMaterialTexture(
                scene,
                aiMaterial,
                modelPath,
                resources,
                aiTextureType_EMISSIVE,
                TextureSettings.defaultAlbedo()
        );

        if (emissiveMap != null) {
            material.setEmissiveMap(emissiveMap);
            material.setEmissive(1.0f,  1.0f, 1.0f);
        }
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
                            "Uncompressed embedded Assimp textures are not supported yet");

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
