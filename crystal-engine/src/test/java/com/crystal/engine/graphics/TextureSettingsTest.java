package com.crystal.engine.graphics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextureSettingsTest {

    @Test
    void defaultAlbedoUsesSrgbAndMipmaps() {
        TextureSettings settings = TextureSettings.defaultAlbedo();

        assertEquals(TextureFormat.SRGBA8, settings.getFormat());
        assertEquals(TextureFilter.LINEAR_MIPMAP_LINEAR, settings.getMinFilter());
        assertEquals(TextureFilter.LINEAR, settings.getMagFilter());
        assertTrue(settings.isGenerateMipmaps());
    }

    @Test
    void defaultDataDoesNotGenerateMipmaps() {
        TextureSettings settings = TextureSettings.defaultData();

        assertEquals(TextureFormat.RGBA8, settings.getFormat());
        assertEquals(TextureFilter.LINEAR, settings.getMinFilter());
        assertEquals(TextureFilter.LINEAR, settings.getMagFilter());
        assertFalse(settings.isGenerateMipmaps());
    }

    @Test
    void forTypeMapsTextureTypesToExpectedDefaults() {
        assertEquals(TextureFormat.SRGBA8, TextureSettings.forType(TextureType.ALBEDO).getFormat());
        assertEquals(TextureFormat.RGBA8, TextureSettings.forType(TextureType.NORMAL).getFormat());
        assertEquals(TextureFormat.RGBA8, TextureSettings.forType(TextureType.DATA).getFormat());
    }

    @Test
    void forTypeRejectsNullTextureType() {
        assertThrows(IllegalArgumentException.class, () -> TextureSettings.forType(null));
    }

    @Test
    void validateRejectsMinFilterMipmapsWhenMipmapsAreDisabled() {
        TextureSettings settings = new TextureSettings()
                .setGenerateMipmaps(false)
                .setMinFilter(TextureFilter.LINEAR_MIPMAP_LINEAR);

        assertThrows(IllegalStateException.class, settings::validate);
    }

    @Test
    void validateRejectsMipmapMagnificationFilter() {
        TextureSettings settings = new TextureSettings()
                .setGenerateMipmaps(true)
                .setMagFilter(TextureFilter.LINEAR_MIPMAP_LINEAR);

        assertThrows(IllegalStateException.class, settings::validate);
    }

    @Test
    void cacheKeyIncludesRelevantSettings() {
        TextureSettings settings = new TextureSettings()
                .setFormat(TextureFormat.RGBA16F)
                .setMinFilter(TextureFilter.NEAREST)
                .setMagFilter(TextureFilter.LINEAR)
                .setWrapS(TextureWrap.CLAMP_TO_EDGE)
                .setWrapT(TextureWrap.REPEAT)
                .setGenerateMipmaps(false);

        assertEquals("RGBA16F:NEAREST:LINEAR:CLAMP_TO_EDGE:REPEAT:false", settings.cacheKey());
    }
}
