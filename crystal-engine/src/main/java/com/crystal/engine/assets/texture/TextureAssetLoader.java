package com.crystal.engine.assets.texture;

import com.crystal.engine.assets.AssetResolver;
import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.texture.TextureDecoder;
import com.crystal.engine.render.texture.TextureSettings;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public final class TextureAssetLoader {

    private final AssetResolver assets;

    public TextureAssetLoader(AssetResolver assets) {
        if (assets == null) throw new IllegalArgumentException("AssetResolver cannot be null");
        this.assets = assets;
    }

    public Texture loadProjectTexture(String path, TextureSettings settings) {
        if (settings == null) throw new IllegalArgumentException("Texture Settings cannot be null");

        return TextureDecoder.load(
            assets.projectTexturePath(path),
            settings
        );
    }

    public Texture loadProjectTexture(String path) {
        return loadProjectTexture(path, TextureSettings.defaultAlbedo());
    }

    public Texture loadProjectDataTexture(String path) {
        return loadProjectTexture(path, TextureSettings.defaultData());
    }

    public Texture loadProjectHDRTexture(String path) {
        return TextureDecoder.loadHDR(
            assets.projectTexturePath(path),
            TextureSettings.defaultHDR()
        );
    }

    public Texture loadPath(Path path, TextureSettings settings) {
        if (path == null) throw new IllegalArgumentException("Path cannot be null");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        return TextureDecoder.load(path, settings);
    }

    public Texture loadEmbedded(String key, ByteBuffer encodedImage, TextureSettings settings) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Key cannot be null or blank");
        if (encodedImage == null) throw new IllegalArgumentException("Encoded image cannot be null");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        return TextureDecoder.loadFromMemory(
            encodedImage,
            settings,
            key
        );
    }

    public String projectTextureCacheKey(String path, TextureSettings settings) {
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        return assets.projectTextureAssetPath(path) + "|" + settings.cacheKey();
    }
}
