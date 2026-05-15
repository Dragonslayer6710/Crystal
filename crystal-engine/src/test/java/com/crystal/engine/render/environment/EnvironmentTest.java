package com.crystal.engine.render.environment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentTest {

    @Test
    void defaultsToNoSkyboxOrIbl() {
        Environment environment = new Environment();

        assertFalse(environment.hasSkybox());
        assertFalse(environment.hasDiffuseIBL());
        assertFalse(environment.hasSpecularIBL());
        assertFalse(environment.hasIBL());
        assertEquals(1.0f, environment.getIblIntensity());
        assertEquals(1.0f, environment.getAmbientIntensity());
    }

    @Test
    void ambientSettersUpdateValuesAndReturnEnvironmentForChaining() {
        Environment environment = new Environment();

        assertSame(environment, environment.setAmbientColor(0.1f, 0.2f, 0.3f));
        assertSame(environment, environment.setAmbientIntensity(0.4f));
        assertSame(environment, environment.setIblIntensity(0.5f));

        assertEquals(0.1f, environment.getAmbientColor().x);
        assertEquals(0.2f, environment.getAmbientColor().y);
        assertEquals(0.3f, environment.getAmbientColor().z);
        assertEquals(0.4f, environment.getAmbientIntensity());
        assertEquals(0.5f, environment.getIblIntensity());
    }
}
