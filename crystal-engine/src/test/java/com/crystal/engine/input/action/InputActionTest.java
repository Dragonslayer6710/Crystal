package com.crystal.engine.input.action;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputActionTest {

    @Test
    void storesActionName() {
        InputAction action = new InputAction("jump");

        assertEquals("jump", action.name());
    }

    @Test
    void rejectsNullOrBlankNames() {
        assertThrows(IllegalArgumentException.class, () -> new InputAction(null));
        assertThrows(IllegalArgumentException.class, () -> new InputAction(""));
        assertThrows(IllegalArgumentException.class, () -> new InputAction(" "));
    }
}
