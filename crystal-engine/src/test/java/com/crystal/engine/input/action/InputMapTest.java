package com.crystal.engine.input.action;

import com.crystal.engine.input.Input;
import com.crystal.engine.input.Key;
import com.crystal.engine.input.MouseButton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputMapTest {

    @Test
    void keyBindingFollowsInputFrameState() {
        Input input = new Input();
        InputAction jump = new InputAction("jump");
        InputMap inputMap = new InputMap().bind(jump, Key.SPACE);

        input.onKey(Key.SPACE, true);

        assertTrue(inputMap.isDown(input, jump));
        assertTrue(inputMap.isPressed(input, jump));
        assertFalse(inputMap.isReleased(input, jump));

        input.beginFrame();

        assertTrue(inputMap.isDown(input, jump));
        assertFalse(inputMap.isPressed(input, jump));
    }

    @Test
    void mouseBindingFollowsInputFrameState() {
        Input input = new Input();
        InputAction fire = new InputAction("fire");
        InputMap inputMap = new InputMap().bind(fire, MouseButton.LMB);

        input.onMouse(MouseButton.LMB, true);

        assertTrue(inputMap.isDown(input, fire));
        assertTrue(inputMap.isPressed(input, fire));

        input.onMouse(MouseButton.LMB, false);

        assertFalse(inputMap.isDown(input, fire));
        assertTrue(inputMap.isReleased(input, fire));
    }

    @Test
    void unboundActionIsInactive() {
        Input input = new Input();
        InputMap inputMap = new InputMap();
        InputAction action = new InputAction("missing");

        assertFalse(inputMap.isDown(input, action));
        assertFalse(inputMap.isPressed(input, action));
        assertFalse(inputMap.isReleased(input, action));
    }

    @Test
    void bindReturnsMapForChainingAndRejectsNulls() {
        InputMap inputMap = new InputMap();
        InputAction action = new InputAction("action");

        assertSame(inputMap, inputMap.bind(action, Key.W));
        assertSame(inputMap, inputMap.bind(action, MouseButton.RMB));

        assertThrows(IllegalArgumentException.class, () -> inputMap.bind(null, Key.W));
        assertThrows(IllegalArgumentException.class, () -> inputMap.bind(action, (Key) null));
        assertThrows(IllegalArgumentException.class, () -> inputMap.bind(null, MouseButton.LMB));
        assertThrows(IllegalArgumentException.class, () -> inputMap.bind(action, (MouseButton) null));
    }

    @Test
    void queryRejectsNulls() {
        InputMap inputMap = new InputMap();
        Input input = new Input();
        InputAction action = new InputAction("action");

        assertThrows(IllegalArgumentException.class, () -> inputMap.isDown(null, action));
        assertThrows(IllegalArgumentException.class, () -> inputMap.isDown(input, null));
        assertThrows(IllegalArgumentException.class, () -> inputMap.isPressed(null, action));
        assertThrows(IllegalArgumentException.class, () -> inputMap.isPressed(input, null));
        assertThrows(IllegalArgumentException.class, () -> inputMap.isReleased(null, action));
        assertThrows(IllegalArgumentException.class, () -> inputMap.isReleased(input, null));
    }
}
