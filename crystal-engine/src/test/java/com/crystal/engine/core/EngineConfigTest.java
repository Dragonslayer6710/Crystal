package com.crystal.engine.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineConfigTest {

    @Test
    void defaultsToExpectedTimingSettings() {
        EngineConfig config = new EngineConfig();

        assertEquals(144, config.getTargetFPS());
        assertEquals(1_000_000_000L / 144, config.getTargetFrameTimeNanos());
        assertEquals(0.25, config.getMaxDeltaTime());
    }

    @Test
    void targetFpsZeroDisablesFixedFrameTime() {
        EngineConfig config = new EngineConfig()
                .setTargetFPS(0);

        assertEquals(0, config.getTargetFPS());
        assertEquals(0, config.getTargetFrameTimeNanos());
    }

    @Test
    void rejectsInvalidTimingValues() {
        EngineConfig config = new EngineConfig();

        assertThrows(IllegalArgumentException.class, () -> config.setTargetFPS(-1));
        assertThrows(IllegalArgumentException.class, () -> config.setMaxDeltaTime(0.0));
    }

    @Test
    void exposesNestedConfigs() {
        EngineConfig config = new EngineConfig();

        assertNotNull(config.getWindowConfig());
        assertNotNull(config.getRendererConfig());
        assertNotNull(config.getAssetConfig());
    }
}
