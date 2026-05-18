package com.crystal.engine.core;

import com.crystal.engine.core.exception.AssetLoadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssetResolverTest {

    @TempDir
    Path assetRoot;

    @Test
    void resolvesProjectAssetPaths() {
        AssetResolver assets = new AssetResolver(new AssetConfig().setAssetRoot(assetRoot));

        assertEquals("shaders/pbr.vert", assets.projectShaderPath("pbr", "vert"));
        assertEquals("engine-assets/shaders/skybox.frag", assets.engineShaderPath("skybox", "frag"));
        assertEquals("textures/bricks_albedo.png", assets.projectTextureAssetPath("bricks_albedo.png"));
        assertEquals(assetRoot.resolve("textures/bricks_albedo.png").toAbsolutePath().normalize(),
                assets.projectTexturePath("bricks_albedo.png"));
        assertEquals(assetRoot.resolve("models/external/DamagedHelmet.glb").toAbsolutePath().normalize(),
                assets.projectModelPath("external/DamagedHelmet.glb"));
    }

    @Test
    void loadsProjectAssetText() throws IOException {
        AssetResolver assets = new AssetResolver(new AssetConfig().setAssetRoot(assetRoot));
        Path shader = assetRoot.resolve("shaders/test.vert");
        Files.createDirectories(shader.getParent());
        Files.writeString(shader, "void main() {}");

        assertEquals("void main() {}", assets.loadProjectAssetAsString("shaders/test.vert"));
    }

    @Test
    void missingProjectAssetReportsAssetLoadException() {
        AssetResolver assets = new AssetResolver(new AssetConfig().setAssetRoot(assetRoot));

        assertThrows(AssetLoadException.class, () -> assets.loadProjectAssetAsString("missing.vert"));
    }

    @Test
    void missingEngineAssetReportsAssetLoadException() {
        AssetResolver assets = new AssetResolver(new AssetConfig().setAssetRoot(assetRoot));

        assertThrows(AssetLoadException.class, () -> assets.loadEngineAssetAsString("engine-assets/missing.vert"));
    }
}
