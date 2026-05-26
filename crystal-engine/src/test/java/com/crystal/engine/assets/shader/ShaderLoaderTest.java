package com.crystal.engine.assets.shader;

import com.crystal.engine.assets.AssetConfig;
import com.crystal.engine.assets.AssetResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShaderLoaderTest {

    @Test
    void projectShaderCacheKeyUsesProjectShaderPaths() {
        ShaderLoader loader = new ShaderLoader(new AssetResolver(new AssetConfig()));

        assertEquals(
            "shaders/pbr.vert|shaders/pbr.frag",
            loader.projectShaderCacheKey("pbr", "pbr")
        );
    }

    @Test
    void engineShaderCacheKeyUsesBundledEngineShaderPaths() {
        ShaderLoader loader = new ShaderLoader(new AssetResolver(new AssetConfig()));

        assertEquals(
            "engine-assets/shaders/skybox.vert|engine-assets/shaders/skybox.frag",
            loader.engineShaderCacheKey("skybox", "skybox")
        );
    }

    @Test
    void shaderCacheKeysRejectNullOrBlankNames() {
        ShaderLoader loader = new ShaderLoader(new AssetResolver(new AssetConfig()));

        assertThrows(IllegalArgumentException.class, () -> loader.projectShaderCacheKey(null, "frag"));
        assertThrows(IllegalArgumentException.class, () -> loader.projectShaderCacheKey("", "frag"));
        assertThrows(IllegalArgumentException.class, () -> loader.projectShaderCacheKey(" ", "frag"));
        assertThrows(IllegalArgumentException.class, () -> loader.projectShaderCacheKey("vert", null));

        assertThrows(IllegalArgumentException.class, () -> loader.engineShaderCacheKey(null, "frag"));
        assertThrows(IllegalArgumentException.class, () -> loader.engineShaderCacheKey("vert", null));
    }
}
