package com.crystal.engine.input.action;

import com.crystal.engine.input.Input;
import com.crystal.engine.input.Key;
import com.crystal.engine.input.MouseButton;

import java.util.HashMap;
import java.util.Map;

public class InputMap {

    private final Map<InputAction, Key> keyBindings = new HashMap<>();
    private final Map<InputAction, MouseButton> mouseBindings = new HashMap<>();

    public InputMap bind(InputAction action, Key key) {
        if (action == null) throw new IllegalArgumentException("Input action cannot be null");
        if (key == null) throw new IllegalArgumentException("Key cannot be null");

        keyBindings.put(action, key);
        return this;
    }

    public InputMap bind(InputAction action, MouseButton button) {
        if (action == null) throw new IllegalArgumentException("Input action cannot be null");
        if (button == null) throw new IllegalArgumentException("Mouse button cannot be null");

        mouseBindings.put(action, button);
        return this;
    }

    public boolean isDown(Input input, InputAction action) {
        if (input == null) throw new IllegalArgumentException("Input cannot be null");
        if (action == null) throw new IllegalArgumentException("Input action cannot be null");

        Key key = keyBindings.get(action);
        if (key != null)
            return input.isKeyDown(key);

        MouseButton button = mouseBindings.get(action);
        return button != null && input.isMouseDown(button);
    }

    public boolean isPressed(Input input, InputAction action) {
        if (input == null) throw new IllegalArgumentException("Input cannot be null");
        if (action == null) throw new IllegalArgumentException("Input action cannot be null");

        Key key = keyBindings.get(action);
        if (key != null)
            return input.isKeyPressed(key);

        MouseButton button = mouseBindings.get(action);
        return button != null && input.isMousePressed(button);
    }

    public boolean isReleased(Input input, InputAction action) {
        if (input == null) throw new IllegalArgumentException("Input cannot be null");
        if (action == null) throw new IllegalArgumentException("Input action cannot be null");

        Key key = keyBindings.get(action);
        if (key != null)
            return input.isKeyReleased(key);

        MouseButton button = mouseBindings.get(action);
        return button != null && input.isMouseReleased(button);
    }
}
