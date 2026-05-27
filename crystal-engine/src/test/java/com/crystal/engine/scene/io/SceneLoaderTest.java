package com.crystal.engine.scene.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SceneLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void readDefinitionParsesLightComponentsWithoutCreatingRenderResources() throws IOException {
        Path scenePath = tempDir.resolve("lights.scene.json");
        Files.writeString(scenePath, """
            {
              "name": "Light Test",
              "version": 7,
              "objects": [
                {
                  "name": "Sun",
                  "type": "empty",
                  "components": [
                    {
                      "type": "directionalLight",
                      "direction": [-1.0, -0.5, -0.25],
                      "color": [0.9, 0.8, 0.7],
                      "intensity": 2.5,
                      "shadowStrength": 0.75,
                      "useTransformDirection": true
                    }
                  ]
                },
                {
                  "name": "Lamp",
                  "type": "empty",
                  "position": [1.0, 2.0, 3.0],
                  "components": [
                    {
                      "type": "pointLight",
                      "color": [1.0, 0.4, 0.2],
                      "intensity": 12.0,
                      "radius": 6.0
                    }
                  ]
                }
              ]
            }
            """);

        SceneDefinition definition = SceneLoader.readDefinition(scenePath);

        assertEquals("Light Test", definition.name);
        assertEquals(7, definition.version);
        assertEquals(2, definition.objects.size());

        SceneDefinition.ComponentDefinition sun = definition.objects.get(0).components.getFirst();
        assertEquals("directionalLight", sun.type);
        assertEquals(List.of(-1.0f, -0.5f, -0.25f), sun.direction);
        assertEquals(List.of(0.9f, 0.8f, 0.7f), sun.color);
        assertEquals(2.5f, sun.intensity);
        assertEquals(0.75f, sun.shadowStrength);
        assertEquals(true, sun.useTransformDirection);

        SceneDefinition.ObjectDefinition lampObject = definition.objects.get(1);
        assertEquals(List.of(1.0f, 2.0f, 3.0f), lampObject.position);

        SceneDefinition.ComponentDefinition lamp = lampObject.components.getFirst();
        assertEquals("pointLight", lamp.type);
        assertEquals(List.of(1.0f, 0.4f, 0.2f), lamp.color);
        assertEquals(12.0f, lamp.intensity);
        assertEquals(6.0f, lamp.radius);
    }

    @Test
    void readDefinitionRejectsNullPath() {
        assertThrows(IllegalArgumentException.class, () -> SceneLoader.readDefinition(null));
    }

    @Test
    void readDefinitionReportsMissingFile() {
        Path missingPath = tempDir.resolve("missing.scene.json");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SceneLoader.readDefinition(missingPath)
        );

        assertEquals("Failed to load scene: " + missingPath, exception.getMessage());
    }
}
