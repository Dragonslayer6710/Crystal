package com.crystal.engine.assets;

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
        assertEquals("textures/Bricks_085/Bricks085_1K-JPG_Color.jpg",
            assets.projectTextureAssetPath("Bricks_085/Bricks085_1K-JPG_Color.jpg"));
        assertEquals(assetRoot.resolve("textures/Bricks_085/Bricks085_1K-JPG_Color.jpg").toAbsolutePath().normalize(),
                assets.projectTexturePath("Bricks_085/Bricks085_1K-JPG_Color.jpg"));
        assertEquals(assetRoot.resolve("models/external/DamagedHelmet.glb").toAbsolutePath().normalize(),
                assets.projectModelPath("external/DamagedHelmet.glb"));
    }

    @Test
    void rejectsNullOrBlankAssetNamesAndPaths() {
        AssetResolver assets = new AssetResolver(new AssetConfig().setAssetRoot(assetRoot));

        assertThrows(IllegalArgumentException.class, () -> assets.projectShaderPath(null, "vert"));
        assertThrows(IllegalArgumentException.class, () -> assets.projectShaderPath(" ", "vert"));
        assertThrows(IllegalArgumentException.class, () -> assets.projectShaderPath("pbr", null));
        assertThrows(IllegalArgumentException.class, () -> assets.engineShaderPath(null, "frag"));
        assertThrows(IllegalArgumentException.class, () -> assets.engineShaderPath("skybox", " "));
        assertThrows(IllegalArgumentException.class, () -> assets.projectTextureAssetPath(null));
        assertThrows(IllegalArgumentException.class, () -> assets.projectTexturePath(" "));
        assertThrows(IllegalArgumentException.class, () -> assets.projectModelPath(null));
        assertThrows(IllegalArgumentException.class, () -> assets.loadProjectAssetAsString(" "));
        assertThrows(IllegalArgumentException.class, () -> assets.loadEngineAssetAsString(null));
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
