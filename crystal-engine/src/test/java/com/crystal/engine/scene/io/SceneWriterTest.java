package com.crystal.engine.scene.io;

import com.crystal.engine.render.RenderLayers;
import com.crystal.engine.scene.Scene;
import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.scene.Transform;
import com.crystal.engine.scene.animation.TransformKeyframe;
import com.crystal.engine.scene.collision.TriggerVolume;
import com.crystal.engine.scene.component.BobComponent;
import com.crystal.engine.scene.component.DirectionalLightComponent;
import com.crystal.engine.scene.component.KeyframeAnimationComponent;
import com.crystal.engine.scene.component.OrbitComponent;
import com.crystal.engine.scene.component.PointLightComponent;
import com.crystal.engine.scene.component.RotationComponent;
import com.crystal.engine.scene.source.SceneObjectSource;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writeExportsRuntimeMotionComponents() {
        Path scenePath = tempDir.resolve("runtime-motion.scene.json");
        Scene scene = new Scene();

        SceneObject mover = new SceneObject("Mover", null, null, new Transform())
            .addComponent(new BobComponent(0.75f, 2.0f).setPhase(0.5f))
            .addComponent(new OrbitComponent(3.0f, 1.5f)
                .setCenter(1.0f, 2.0f, 3.0f)
                .setPhase(0.25f));

        scene.add(mover);

        SceneWriter.write(scenePath, scene);

        SceneDefinition definition = SceneLoader.readDefinition(scenePath);
        SceneDefinition.ObjectDefinition object = definition.objects.getFirst();

        assertEquals(2, object.components.size());

        SceneDefinition.ComponentDefinition bob = object.components.get(0);
        assertEquals("bob", bob.type);
        assertEquals(0.75f, bob.amplitude);
        assertEquals(2.0f, bob.speed);
        assertEquals(0.5f, bob.phase);

        SceneDefinition.ComponentDefinition orbit = object.components.get(1);
        assertEquals("orbit", orbit.type);
        assertEquals(List.of(1.0f, 2.0f, 3.0f), orbit.center);
        assertEquals(3.0f, orbit.radius);
        assertEquals(1.5f, orbit.speed);
        assertEquals(0.25f, orbit.phase);
    }

    @Test
    void writeDefinitionRoundTripsMotionComponentsThroughSceneDefinition() {
        Path scenePath = tempDir.resolve("motion.scene.json");
        SceneDefinition definition = new SceneDefinition();

        SceneDefinition.ObjectDefinition mover = new SceneDefinition.ObjectDefinition();
        mover.name = "Mover";
        mover.type = "empty";

        SceneDefinition.ComponentDefinition bob = new SceneDefinition.ComponentDefinition();
        bob.type = "bob";
        bob.amplitude = 0.75f;
        bob.speed = 2.0f;
        bob.phase = 0.5f;

        SceneDefinition.ComponentDefinition orbit = new SceneDefinition.ComponentDefinition();
        orbit.type = "orbit";
        orbit.center = List.of(1.0f, 2.0f, 3.0f);
        orbit.radius = 3.0f;
        orbit.speed = 1.5f;
        orbit.phase = 0.25f;

        mover.components = List.of(bob, orbit);
        definition.objects = List.of(mover);

        SceneWriter.writeDefinition(scenePath, definition);

        SceneDefinition loaded = SceneLoader.readDefinition(scenePath);
        SceneDefinition.ObjectDefinition loadedMover = loaded.objects.getFirst();

        assertEquals(2, loadedMover.components.size());

        SceneDefinition.ComponentDefinition loadedBob = loadedMover.components.get(0);
        assertEquals("bob", loadedBob.type);
        assertEquals(0.75f, loadedBob.amplitude);
        assertEquals(2.0f, loadedBob.speed);
        assertEquals(0.5f, loadedBob.phase);

        SceneDefinition.ComponentDefinition loadedOrbit = loadedMover.components.get(1);
        assertEquals("orbit", loadedOrbit.type);
        assertEquals(List.of(1.0f, 2.0f, 3.0f), loadedOrbit.center);
        assertEquals(3.0f, loadedOrbit.radius);
        assertEquals(1.5f, loadedOrbit.speed);
        assertEquals(0.25f, loadedOrbit.phase);
    }

    @Test
    void writeExportsRuntimeSceneObjectMetadataHierarchyAndComponents() {
        Path scenePath = tempDir.resolve("runtime-object.scene.json");
        Scene scene = new Scene();

        SceneObject parent = new SceneObject(
            "Parent",
            null,
            null,
            new Transform()
                .setPosition(1.0f, 2.0f, 3.0f)
                .setRotationDegrees(10.0f, 20.0f, 30.0f)
                .setScale(2.0f, 3.0f, 4.0f)
        )
            .setSource(SceneObjectSource.primitive("cube", "brick"))
            .addTag("debug")
            .addTag("interactive")
            .setLayerMask(RenderLayers.DEBUG | RenderLayers.EDITOR)
            .setCastsShadow(false)
            .setTriggerVolume(new TriggerVolume(0.5f, 1.0f, 1.5f))
            .addComponent(new RotationComponent(0.1f, 0.2f, 0.3f))
            .addComponent(new KeyframeAnimationComponent(List.of(
                new TransformKeyframe(
                    0.0,
                    new Vector3f(1.0f, 2.0f, 3.0f),
                    new Vector3f(0.0f, 45.0f, 0.0f),
                    new Vector3f(1.0f, 1.0f, 1.0f)
                ),
                new TransformKeyframe(
                    1.5,
                    new Vector3f(2.0f, 3.0f, 4.0f),
                    new Vector3f(0.0f, 90.0f, 0.0f),
                    new Vector3f(2.0f, 2.0f, 2.0f)
                )
            )).setLoop(false));

        SceneObject child = new SceneObject(
            "Child",
            null,
            null,
            new Transform().setPosition(0.0f, 1.0f, 0.0f)
        )
            .setSource(SceneObjectSource.empty())
            .addTag("child");

        parent.addChild(child);
        scene.add(parent);

        SceneWriter.write(scenePath, scene);

        SceneDefinition definition = SceneLoader.readDefinition(scenePath);

        assertEquals(1, definition.objects.size());

        SceneDefinition.ObjectDefinition exportedParent = definition.objects.getFirst();
        assertEquals("Parent", exportedParent.name);
        assertEquals("primitive", exportedParent.type);
        assertEquals("cube", exportedParent.primitive);
        assertEquals("brick", exportedParent.material);
        assertEquals(List.of(1.0f, 2.0f, 3.0f), exportedParent.position);
        assertFloatListEquals(List.of(10.0f, 20.0f, 30.0f), exportedParent.rotationDegrees);
        assertEquals(List.of(2.0f, 3.0f, 4.0f), exportedParent.scale);
        assertTrue(exportedParent.tags.contains("debug"));
        assertTrue(exportedParent.tags.contains("interactive"));
        assertEquals(RenderLayers.DEBUG | RenderLayers.EDITOR, exportedParent.layerMask);
        assertEquals(false, exportedParent.castsShadow);
        assertEquals(List.of(0.5f, 1.0f, 1.5f), exportedParent.trigger.halfExtents);

        assertEquals(2, exportedParent.components.size());

        SceneDefinition.ComponentDefinition rotation = exportedParent.components.get(0);
        assertEquals("rotation", rotation.type);
        assertEquals(List.of(0.1f, 0.2f, 0.3f), rotation.speedRadiansPerSecond);

        SceneDefinition.ComponentDefinition animation = exportedParent.components.get(1);
        assertEquals("keyframeAnimation", animation.type);
        assertEquals(false, animation.loop);
        assertEquals(2, animation.keyframes.size());
        assertEquals(0.0, animation.keyframes.get(0).time);
        assertEquals(List.of(1.0f, 2.0f, 3.0f), animation.keyframes.get(0).position);
        assertEquals(List.of(0.0f, 45.0f, 0.0f), animation.keyframes.get(0).rotationDegrees);
        assertEquals(List.of(1.0f, 1.0f, 1.0f), animation.keyframes.get(0).scale);
        assertEquals(1.5, animation.keyframes.get(1).time);
        assertEquals(List.of(2.0f, 3.0f, 4.0f), animation.keyframes.get(1).position);
        assertEquals(List.of(0.0f, 90.0f, 0.0f), animation.keyframes.get(1).rotationDegrees);
        assertEquals(List.of(2.0f, 2.0f, 2.0f), animation.keyframes.get(1).scale);

        assertEquals(1, exportedParent.children.size());

        SceneDefinition.ObjectDefinition exportedChild = exportedParent.children.getFirst();
        assertEquals("Child", exportedChild.name);
        assertEquals("empty", exportedChild.type);
        assertEquals(List.of(0.0f, 1.0f, 0.0f), exportedChild.position);
        assertEquals(List.of("child"), exportedChild.tags);
    }

    @Test
    void writeDoesNotExpandImportedModelChildren() {
        Path scenePath = tempDir.resolve("model-root.scene.json");
        Scene scene = new Scene();

        SceneObject modelRoot = new SceneObject("Model", null, null, new Transform())
            .setSource(SceneObjectSource.model("external/model.glb"));

        modelRoot.addChild(new SceneObject(
            "Imported Child",
            null,
            null,
            new Transform().setPosition(1.0f, 0.0f, 0.0f)
        ).setSource(SceneObjectSource.empty()));

        scene.add(modelRoot);

        SceneWriter.write(scenePath, scene);

        SceneDefinition definition = SceneLoader.readDefinition(scenePath);

        SceneDefinition.ObjectDefinition exportedModel = definition.objects.getFirst();
        assertEquals("Model", exportedModel.name);
        assertEquals("model", exportedModel.type);
        assertEquals("external/model.glb", exportedModel.path);
        assertEquals(null, exportedModel.children);
    }

    @Test
    void writeExportsRuntimeSceneLightComponents() {
        Path scenePath = tempDir.resolve("runtime-lights.scene.json");
        Scene scene = new Scene();

        DirectionalLightComponent directionalLight = new DirectionalLightComponent()
            .setDirection(-1.0f, -0.5f, -0.25f)
            .setColor(1.0f, 0.85f, 0.6f)
            .setIntensity(3.5f)
            .setShadowStrength(0.4f)
            .setUseTransformDirection(true);

        SceneObject sun = new SceneObject("Sun", null, null, new Transform())
            .addComponent(directionalLight);

        PointLightComponent pointLight = new PointLightComponent()
            .setColor(1.0f, 0.25f, 0.1f)
            .setIntensity(7.0f)
            .setRadius(3.0f);

        SceneObject lamp = new SceneObject("Lamp", null, null, new Transform().setPosition(1.0f, 2.0f, 3.0f))
            .addComponent(pointLight);

        scene.add(sun);
        scene.add(lamp);

        SceneWriter.write(scenePath, scene);

        SceneDefinition definition = SceneLoader.readDefinition(scenePath);

        assertEquals(2, definition.objects.size());

        SceneDefinition.ComponentDefinition exportedSun = definition.objects.get(0).components.getFirst();
        assertEquals("directionalLight", exportedSun.type);
        assertFloatListEquals(List.of(-0.8728716f, -0.4364358f, -0.2182179f), exportedSun.direction);
        assertEquals(List.of(1.0f, 0.85f, 0.6f), exportedSun.color);
        assertEquals(3.5f, exportedSun.intensity);
        assertEquals(0.4f, exportedSun.shadowStrength);
        assertEquals(true, exportedSun.useTransformDirection);

        SceneDefinition.ObjectDefinition exportedLampObject = definition.objects.get(1);
        assertEquals(List.of(1.0f, 2.0f, 3.0f), exportedLampObject.position);

        SceneDefinition.ComponentDefinition exportedLamp = exportedLampObject.components.getFirst();
        assertEquals("pointLight", exportedLamp.type);
        assertEquals(List.of(1.0f, 0.25f, 0.1f), exportedLamp.color);
        assertEquals(7.0f, exportedLamp.intensity);
        assertEquals(3.0f, exportedLamp.radius);
    }

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

    private static void assertFloatListEquals(List<Float> expected, List<Float> actual) {
        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            float expectedValue = expected.get(i);
            float actualValue = actual.get(i);

            assertTrue(
                Math.abs(expectedValue - actualValue) < 0.0001f,
                "Expected " + expectedValue + " but was " + actualValue + " at index " + i
            );
        }
    }
}
