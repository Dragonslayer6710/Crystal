package com.crystal.engine.assets.texture;

import com.crystal.engine.assets.AssetConfig;
import com.crystal.engine.assets.AssetResolver;
import com.crystal.engine.render.texture.TextureSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextureAssetLoaderTest {

    @Test
    void projectTextureCacheKeyPrefixesTextureRootOnce() {
        TextureAssetLoader loader = new TextureAssetLoader(new AssetResolver(new AssetConfig()));
        TextureSettings settings = TextureSettings.defaultAlbedo();

        String cacheKey = loader.projectTextureCacheKey("Bricks/color.jpg", settings);

        assertEquals("textures/Bricks/color.jpg|" + settings.cacheKey(), cacheKey);
    }

    @Test
    void projectTextureCacheKeyRejectsNullSettings() {
        TextureAssetLoader loader = new TextureAssetLoader(new AssetResolver(new AssetConfig()));

        assertThrows(
            IllegalArgumentException.class,
            () -> loader.projectTextureCacheKey("texture.png", null)
        );
    }

    @Test
    void projectTextureCacheKeyRejectsNullOrBlankPath() {
        TextureAssetLoader loader = new TextureAssetLoader(new AssetResolver(new AssetConfig()));
        TextureSettings settings = TextureSettings.defaultAlbedo();

        assertThrows(IllegalArgumentException.class, () -> loader.projectTextureCacheKey(null, settings));
        assertThrows(IllegalArgumentException.class, () -> loader.projectTextureCacheKey("", settings));
        assertThrows(IllegalArgumentException.class, () -> loader.projectTextureCacheKey(" ", settings));
    }
}
