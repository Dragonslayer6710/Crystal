package com.crystal.engine.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AssetConfigTest {

    @Test
    void defaultsToAssetsDirectory() {
        AssetConfig config = new AssetConfig();

        assertEquals(Path.of("assets"), config.getAssetRoot());
    }

    @Test
    void acceptsPathAndStringAssetRoots() {
        AssetConfig config = new AssetConfig();

        assertSame(config, config.setAssetRoot(Path.of("content")));
        assertEquals(Path.of("content"), config.getAssetRoot());

        assertSame(config, config.setAssetRoot("assets-dev"));
        assertEquals(Path.of("assets-dev"), config.getAssetRoot());
    }

    @Test
    void rejectsNullOrBlankAssetRoots() {
        AssetConfig config = new AssetConfig();

        assertThrows(IllegalArgumentException.class, () -> config.setAssetRoot((Path) null));
        assertThrows(IllegalArgumentException.class, () -> config.setAssetRoot((String) null));
        assertThrows(IllegalArgumentException.class, () -> config.setAssetRoot(" "));
    }
}
