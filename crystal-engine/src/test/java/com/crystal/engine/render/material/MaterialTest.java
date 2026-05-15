package com.crystal.engine.render.material;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaterialTest {

    @Test
    void defaultsToNeutralMaterialValues() {
        Material material = new Material(null);

        assertEquals(1.0f, material.getTint().x);
        assertEquals(1.0f, material.getTint().y);
        assertEquals(1.0f, material.getTint().z);
        assertEquals(0.5f, material.getRoughness());
        assertEquals(0.0f, material.getMetallic());
        assertEquals(0.0f, material.getEmissive().x);
        assertEquals(0.0f, material.getEmissive().y);
        assertEquals(0.0f, material.getEmissive().z);
    }

    @Test
    void setRoughnessRejectsValuesBelowZero() {
        Material material = new Material(null);

        assertThrows(IllegalArgumentException.class, () -> material.setRoughness(-0.1f));
    }

    @Test
    void setRoughnessRejectsValuesAboveOne() {
        Material material = new Material(null);

        assertThrows(IllegalArgumentException.class, () -> material.setRoughness(1.1f));
    }

    @Test
    void setMetallicRejectsValuesBelowZero() {
        Material material = new Material(null);

        assertThrows(IllegalArgumentException.class, () -> material.setMetallic(-0.1f));
    }

    @Test
    void setMetallicRejectsValuesAboveOne() {
        Material material = new Material(null);

        assertThrows(IllegalArgumentException.class, () -> material.setMetallic(1.1f));
    }

    @Test
    void scalarSettersReturnMaterialForChaining() {
        Material material = new Material(null);

        assertSame(material, material.setTint(0.5f, 0.6f, 0.7f));
        assertSame(material, material.setRoughness(0.75f));
        assertSame(material, material.setMetallic(0.25f));
        assertSame(material, material.setEmissive(1.0f, 0.5f, 0.0f));
    }
}
