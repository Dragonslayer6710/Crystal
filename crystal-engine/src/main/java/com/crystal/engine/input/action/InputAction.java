package com.crystal.engine.input.action;

public record InputAction(String name) {

    public InputAction {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Input action name cannot be null or blank");
    }
}
