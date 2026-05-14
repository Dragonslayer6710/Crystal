package com.crystal.engine.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputTest {

    @Test
    void keyPressSetsPressedAndDownUntilNextFrame() {
        Input input = new Input();

        input.onKey(Key.W, true);

        assertTrue(input.isKeyPressed(Key.W));
        assertTrue(input.isKeyDown(Key.W));

        input.beginFrame();

        assertFalse(input.isKeyPressed(Key.W));
        assertTrue(input.isKeyDown(Key.W));
    }

    @Test
    void keyReleaseSetsReleasedAndClearsDownUntilNextFrame() {
        Input input = new Input();

        input.onKey(Key.W, true);
        input.beginFrame();
        input.onKey(Key.W, false);

        assertTrue(input.isKeyReleased(Key.W));
        assertFalse(input.isKeyDown(Key.W));

        input.beginFrame();

        assertFalse(input.isKeyReleased(Key.W));
        assertFalse(input.isKeyDown(Key.W));
    }

    @Test
    void repeatedKeyPressDoesNotRepeatPressedWhileHeld() {
        Input input = new Input();

        input.onKey(Key.W, true);
        input.beginFrame();
        input.onKey(Key.W, true);

        assertFalse(input.isKeyPressed(Key.W));
        assertTrue(input.isKeyDown(Key.W));
    }

    @Test
    void mouseButtonPressAndReleaseFollowFrameState() {
        Input input = new Input();

        input.onMouse(MouseButton.LMB, true);

        assertTrue(input.isMousePressed(MouseButton.LMB));
        assertTrue(input.isMouseDown(MouseButton.LMB));

        input.beginFrame();
        input.onMouse(MouseButton.LMB, false);

        assertTrue(input.isMouseReleased(MouseButton.LMB));
        assertFalse(input.isMouseDown(MouseButton.LMB));
    }

    @Test
    void firstMouseMoveDoesNotCreateDelta() {
        Input input = new Input();

        input.onMouseMove(10.0, 20.0);

        assertEquals(0.0, input.getMouseDeltaX());
        assertEquals(0.0, input.getMouseDeltaY());
    }

    @Test
    void mouseMoveAccumulatesDeltaUntilBeginFrame() {
        Input input = new Input();

        input.onMouseMove(10.0, 20.0);
        input.onMouseMove(13.0, 25.0);
        input.onMouseMove(18.0, 21.0);

        assertEquals(8.0, input.getMouseDeltaX());
        assertEquals(1.0, input.getMouseDeltaY());

        input.beginFrame();

        assertEquals(0.0, input.getMouseDeltaX());
        assertEquals(0.0, input.getMouseDeltaY());
    }
}
