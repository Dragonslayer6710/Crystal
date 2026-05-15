package com.crystal.engine.render.material;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MaterialTest {

    @Test
    void constructorRejectsNullShader() {
        assertThrows(IllegalArgumentException.class, () -> new Material(null));
    }
}
