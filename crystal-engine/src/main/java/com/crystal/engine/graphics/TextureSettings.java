package com.crystal.engine.graphics;

public final class TextureSettings {

    private TextureFormat format = TextureFormat.RGBA8;
    private TextureFilter minFilter = TextureFilter.LINEAR;
    private TextureFilter magFilter = TextureFilter.LINEAR;
    private TextureWrap wrapS = TextureWrap.REPEAT;
    private TextureWrap wrapT = TextureWrap.REPEAT;
    private boolean generateMipmaps = false;

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
}
