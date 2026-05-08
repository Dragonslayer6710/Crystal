package com.crystal.engine.core;

import java.nio.file.Path;

public final class AssetConfig {

    private Path assetRoot = Path.of("assets");

    public Path getAssetRoot() {
        return assetRoot;
    }

    public AssetConfig setAssetRoot(Path assetRoot) {
        if (assetRoot == null) {
            throw new IllegalArgumentException("Asset root cannot be null");
        }

        this.assetRoot = assetRoot;
        return this;
    }

    public AssetConfig setAssetRoot(String assetRoot) {
        if (assetRoot == null || assetRoot.isBlank()) {
            throw new IllegalArgumentException("Asset root cannot be null or blank");
        }

        return setAssetRoot(Path.of(assetRoot));
    }
}