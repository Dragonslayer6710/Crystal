package com.crystal.engine.assets.model.assimp;

import com.crystal.engine.assets.model.ModelLoadOptions;
import com.crystal.engine.assets.ResourceManager;
import com.crystal.engine.core.exception.ModelLoadException;
import com.crystal.engine.render.texture.TextureSettings;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.texture.Texture;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.assimp.Assimp.*;

final class AssimpMaterialLoader {

    private static final Logger logger = LoggerFactory.getLogger(AssimpMaterialLoader.class);

    private AssimpMaterialLoader() {}

    static Material createMaterial(AIScene scene, AIMesh aiMesh, Path modelPath,
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
            material.setEmissive(1.0f, 1.0f, 1.0f);
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
                int embeddedIndex = parseEmbeddedTextureIndex(textureRef, modelPath);

                PointerBuffer textures = scene.mTextures();
                if (textures == null || embeddedIndex >= scene.mNumTextures())
                    return null;

                AITexture embedded = AITexture.create(textures.get(embeddedIndex));

                if (embedded.mHeight() != 0)
                    throw new ModelLoadException("Failed to load model '" + modelPath
                            + "': uncompressed embedded Assimp textures are not supported yet");

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

            Path parent = modelPath.getParent();
            if (parent == null) {
                throw new ModelLoadException("Failed to resolve material texture '" + textureRef
                        + "' for model without a parent path: " + modelPath);
            }

            Path resolved = parent.resolve(textureRef).normalize();
            return resources.loadTexture(resolved, settings);
        }
    }

    private static int parseEmbeddedTextureIndex(String textureRef, Path modelPath) {
        try {
            return Integer.parseInt(textureRef.substring(1));
        } catch (NumberFormatException e) {
            throw new ModelLoadException("Failed to load model '" + modelPath
                    + "': invalid embedded texture reference " + textureRef, e);
        }
    }
}
