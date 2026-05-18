package com.crystal.engine.core;

import com.crystal.engine.core.exception.AssetLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class AssetResolver {

    private static final String ENGINE_ASSET_ROOT = "engine-assets";

    private final Path assetRoot;

    AssetResolver(AssetConfig config) {
        if (config == null) throw new IllegalArgumentException("AssetConfig cannot be null");
        this.assetRoot = config.getAssetRoot().toAbsolutePath().normalize();
    }

    String projectShaderPath(String name, String extension) {
        return "shaders/" + name + "." + extension;
    }

    String engineShaderPath(String name, String extension) {
        return ENGINE_ASSET_ROOT + "/shaders/" + name + "." + extension;
    }

    Path projectTexturePath(String path) {
        return assetRoot.resolve(projectTextureAssetPath(path));
    }

    String projectTextureAssetPath(String path) {
        return "textures/" + path;
    }

    Path projectModelPath(String path) {
        return assetRoot.resolve("models/" + path);
    }

    String loadProjectAssetAsString(String path) {
        Path fullPath = assetRoot.resolve(path);

        try {
            return Files.readString(fullPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssetLoadException("Failed to load asset: " + fullPath.toAbsolutePath(), e);
        }
    }

    String loadEngineAssetAsString(String path) {
        try (InputStream stream = AssetResolver.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null)
                throw new AssetLoadException("Failed to load engine asset: " + path);

            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssetLoadException("Failed to load engine asset: " + path, e);
        }
    }
}
