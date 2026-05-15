package com.crystal.engine.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RendererConfigTest {

    @Test
    void defaultsToCommonRenderFeaturesEnabled() {
        RendererConfig config = new RendererConfig();

        assertTrue(config.isDepthTest());
        assertTrue(config.isFaceCulling());
        assertTrue(config.isFrustumCulling());
        assertTrue(config.isShadowsEnabled());
        assertEquals(2048, config.getShadowMapSize());
    }

    @Test
    void settersUpdateValuesAndReturnConfigForChaining() {
        RendererConfig config = new RendererConfig();

        assertSame(config, config.setDepthTest(false));
        assertSame(config, config.setFaceCulling(false));
        assertSame(config, config.setFrustumCulling(false));
        assertSame(config, config.setShadowsEnabled(false));
        assertSame(config, config.setShadowMapSize(1024));

        assertFalse(config.isDepthTest());
        assertFalse(config.isFaceCulling());
        assertFalse(config.isFrustumCulling());
        assertFalse(config.isShadowsEnabled());
        assertEquals(1024, config.getShadowMapSize());
    }

    @Test
    void rejectsInvalidShadowMapSize() {
        RendererConfig config = new RendererConfig();

        assertThrows(IllegalArgumentException.class, () -> config.setShadowMapSize(0));
        assertThrows(IllegalArgumentException.class, () -> config.setShadowMapSize(-1));
    }
}
