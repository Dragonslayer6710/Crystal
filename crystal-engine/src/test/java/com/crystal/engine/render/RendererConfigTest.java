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
    }

    @Test
    void settersUpdateValuesAndReturnConfigForChaining() {
        RendererConfig config = new RendererConfig();

        assertSame(config, config.setDepthTest(false));
        assertSame(config, config.setFaceCulling(false));
        assertSame(config, config.setFrustumCulling(false));

        assertFalse(config.isDepthTest());
        assertFalse(config.isFaceCulling());
        assertFalse(config.isFrustumCulling());
    }
}
