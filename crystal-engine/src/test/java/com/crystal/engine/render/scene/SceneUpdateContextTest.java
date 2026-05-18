package com.crystal.engine.render.scene;

import com.crystal.engine.input.Input;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SceneUpdateContextTest {

    @Test
    void storesFrameServicesForComponents() {
        Input input = new Input();
        Window window = new Window(new WindowConfig());

        SceneUpdateContext context = new SceneUpdateContext(0.25, input, window);

        assertEquals(0.25, context.getDeltaTime());
        assertSame(input, context.getInput());
        assertSame(window, context.getWindow());
    }

    @Test
    void rejectsNullInput() {
        Window window = new Window(new WindowConfig());

        assertThrows(
                IllegalArgumentException.class,
                () -> new SceneUpdateContext(0.25, null, window)
        );
    }

    @Test
    void rejectsNullWindow() {
        Input input = new Input();

        assertThrows(
                IllegalArgumentException.class,
                () -> new SceneUpdateContext(0.25, input, null)
        );
    }
}
