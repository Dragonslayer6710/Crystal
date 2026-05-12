package com.crystal.engine.graphics;

public final class TextureSettings {

    private TextureFormat format = TextureFormat.RGBA8;
    private TextureFilter minFilter = TextureFilter.LINEAR;
    private TextureFilter magFilter = TextureFilter.LINEAR;
    private TextureWrap wrapS = TextureWrap.REPEAT;
    private TextureWrap wrapT = TextureWrap.REPEAT;
    private boolean generateMipmaps = false;

    public static TextureSettings defaultAlbedo() {
        return new TextureSettings()
                .setFormat(TextureFormat.SRGBA8)
                .setGenerateMipmaps(true)
                .setMinFilter(TextureFilter.LINEAR_MIPMAP_LINEAR)
                .setMagFilter(TextureFilter.LINEAR);
    }

    public static TextureSettings defaultData() {
        return new TextureSettings()
                .setFormat(TextureFormat.RGBA8)
                .setGenerateMipmaps(false)
                .setMinFilter(TextureFilter.LINEAR)
                .setMagFilter(TextureFilter.LINEAR);
    }

    public static TextureSettings defaultCubemap() {
        return new TextureSettings()
                .setFormat(TextureFormat.RGBA8)
                .setGenerateMipmaps(false)
                .setMinFilter(TextureFilter.LINEAR)
                .setMagFilter(TextureFilter.LINEAR)
                .setWrapS(TextureWrap.CLAMP_TO_EDGE)
                .setWrapT(TextureWrap.CLAMP_TO_EDGE);
    }

    public static TextureSettings defaultHDR() {
        return new TextureSettings()
                .setFormat(TextureFormat.RGBA16F)
                .setGenerateMipmaps(false)
                .setMinFilter(TextureFilter.LINEAR)
                .setMagFilter(TextureFilter.LINEAR)
                .setWrapS(TextureWrap.CLAMP_TO_EDGE)
                .setWrapT(TextureWrap.CLAMP_TO_EDGE);
    }

    public static TextureSettings defaultPrefilterCubemap() {
        return new TextureSettings()
                .setFormat(TextureFormat.RGBA16F)
                .setGenerateMipmaps(true)
                .setMinFilter(TextureFilter.LINEAR_MIPMAP_LINEAR)
                .setMagFilter(TextureFilter.LINEAR)
                .setWrapS(TextureWrap.CLAMP_TO_EDGE)
                .setWrapT(TextureWrap.CLAMP_TO_EDGE);
    }

    public static TextureSettings forType(TextureType type) {
        if (type == null) throw new IllegalArgumentException("TextureType cannot be null");

        return switch (type) {
            case ALBEDO -> defaultAlbedo();
            case NORMAL, DATA -> defaultData();
        };
    }

    public TextureFormat getFormat() { return format; }
    public TextureFilter getMinFilter() { return minFilter; }
    public TextureFilter getMagFilter() { return magFilter; }
    public TextureWrap getWrapS() { return wrapS; }
    public TextureWrap getWrapT() { return wrapT; }
    public boolean isGenerateMipmaps() { return generateMipmaps; }

    public TextureSettings setFormat(TextureFormat format) {
        this.format = format;
        return this;
    }

    public TextureSettings setMinFilter(TextureFilter minFilter) {
        this.minFilter = minFilter;
        return this;
    }

    public TextureSettings setMagFilter(TextureFilter magFilter) {
        this.magFilter = magFilter;
        return this;
    }

    public TextureSettings setWrapS(TextureWrap wrapS) {
        this.wrapS = wrapS;
        return this;
    }

    public TextureSettings setWrapT(TextureWrap wrapT) {
        this.wrapT = wrapT;
        return this;
    }

    public TextureSettings setGenerateMipmaps(boolean generateMipmaps) {
        this.generateMipmaps = generateMipmaps;
        return this;
    }

    public void validate() {
        if (minFilter.usesMipmaps() && !generateMipmaps) throw new IllegalStateException(
                "Min filter requires mipmaps but generateMipmaps is false"
        );

        if (magFilter.usesMipmaps()) {
            throw new IllegalStateException(
                    "Magnification filter cannot use mipmaps"
            );
        }
    }

    public String cacheKey() {
        return format + ":" +
                minFilter + ":" +
                magFilter + ":" +
                wrapS + ":" +
                wrapT + ":" +
                generateMipmaps;
    }
}
