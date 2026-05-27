package com.crystal.engine.scene.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SceneWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writeDefinitionRoundTripsLightComponentsThroughSceneLoader() {
        Path scenePath = tempDir.resolve("lights.scene.json");
        SceneDefinition definition = new SceneDefinition();
        definition.name = "Written Lights";
        definition.version = 3;

        SceneDefinition.ObjectDefinition sun = new SceneDefinition.ObjectDefinition();
        sun.name = "Sun";
        sun.type = "empty";

        SceneDefinition.ComponentDefinition directionalLight = new SceneDefinition.ComponentDefinition();
        directionalLight.type = "directionalLight";
        directionalLight.direction = List.of(-1.0f, -0.5f, -0.25f);
        directionalLight.color = List.of(1.0f, 0.9f, 0.75f);
        directionalLight.intensity = 4.0f;
        directionalLight.shadowStrength = 0.5f;
        directionalLight.useTransformDirection = true;
        sun.components = List.of(directionalLight);

        SceneDefinition.ObjectDefinition lamp = new SceneDefinition.ObjectDefinition();
        lamp.name = "Lamp";
        lamp.type = "empty";
        lamp.position = List.of(1.0f, 2.0f, 3.0f);

        SceneDefinition.ComponentDefinition pointLight = new SceneDefinition.ComponentDefinition();
        pointLight.type = "pointLight";
        pointLight.color = List.of(1.0f, 0.3f, 0.1f);
        pointLight.intensity = 9.0f;
        pointLight.radius = 4.0f;
        lamp.components = List.of(pointLight);

        definition.objects = List.of(sun, lamp);

        SceneWriter.writeDefinition(scenePath, definition);

        SceneDefinition loaded = SceneLoader.readDefinition(scenePath);

        assertEquals("Written Lights", loaded.name);
        assertEquals(3, loaded.version);
        assertEquals(2, loaded.objects.size());

        SceneDefinition.ComponentDefinition loadedSun = loaded.objects.get(0).components.getFirst();
        assertEquals("directionalLight", loadedSun.type);
        assertEquals(List.of(-1.0f, -0.5f, -0.25f), loadedSun.direction);
        assertEquals(List.of(1.0f, 0.9f, 0.75f), loadedSun.color);
        assertEquals(4.0f, loadedSun.intensity);
        assertEquals(0.5f, loadedSun.shadowStrength);
        assertEquals(true, loadedSun.useTransformDirection);

        SceneDefinition.ObjectDefinition loadedLampObject = loaded.objects.get(1);
        assertEquals(List.of(1.0f, 2.0f, 3.0f), loadedLampObject.position);

        SceneDefinition.ComponentDefinition loadedLamp = loadedLampObject.components.getFirst();
        assertEquals("pointLight", loadedLamp.type);
        assertEquals(List.of(1.0f, 0.3f, 0.1f), loadedLamp.color);
        assertEquals(9.0f, loadedLamp.intensity);
        assertEquals(4.0f, loadedLamp.radius);
    }

    @Test
    void writeDefinitionRejectsInvalidArguments() {
        SceneDefinition definition = new SceneDefinition();
        Path scenePath = tempDir.resolve("scene.json");

        assertThrows(IllegalArgumentException.class, () -> SceneWriter.writeDefinition(null, definition));
        assertThrows(IllegalArgumentException.class, () -> SceneWriter.writeDefinition(scenePath, null));
    }
}
