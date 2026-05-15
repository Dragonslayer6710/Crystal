package com.crystal.engine.window;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WindowConfigTest {

    @Test
    void defaultsToExpectedWindowSettings() {
        WindowConfig config = new WindowConfig();

        assertEquals(1280, config.getWidth());
        assertEquals(720, config.getHeight());
        assertEquals("Crystal Engine", config.getTitle());
        assertTrue(config.isVisible());
        assertTrue(config.isResizable());
        assertFalse(config.isVSync());
        assertTrue(config.isDebugContext());
    }

    @Test
    void rejectsInvalidDimensionsAndTitle() {
        WindowConfig config = new WindowConfig();

        assertThrows(IllegalArgumentException.class, () -> config.setWidth(0));
        assertThrows(IllegalArgumentException.class, () -> config.setHeight(0));
        assertThrows(IllegalArgumentException.class, () -> config.setTitle(null));
        assertThrows(IllegalArgumentException.class, () -> config.setTitle(" "));
    }

    @Test
    void settersUpdateValuesAndReturnConfigForChaining() {
        WindowConfig config = new WindowConfig();

        assertSame(config, config.setWidth(1920));
        assertSame(config, config.setHeight(1080));
        assertSame(config, config.setTitle("Test"));
        assertSame(config, config.setVisible(false));
        assertSame(config, config.setResizable(false));
        assertSame(config, config.setVSync(true));
        assertSame(config, config.setDebugContext(false));

        assertEquals(1920, config.getWidth());
        assertEquals(1080, config.getHeight());
        assertEquals("Test", config.getTitle());
        assertFalse(config.isVisible());
        assertFalse(config.isResizable());
        assertTrue(config.isVSync());
        assertFalse(config.isDebugContext());
    }
}
