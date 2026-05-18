package com.crystal.engine.core;

import com.crystal.engine.assets.model.ModelLoadOptions;
import com.crystal.engine.core.exception.AssetLoadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceManagerTest {

    @TempDir
    Path assetRoot;

    @Test
    void registerReturnsTheResource() {
        ResourceManager resources = new ResourceManager();
        TestResource resource = new TestResource("resource", new ArrayList<>());

        assertSame(resource, resources.register(resource));
    }

    @Test
    void disposeAllDisposesResourcesInReverseRegistrationOrder() {
        ResourceManager resources = new ResourceManager();
        List<String> disposed = new ArrayList<>();

        resources.register(new TestResource("first", disposed));
        resources.register(new TestResource("second", disposed));
        resources.register(new TestResource("third", disposed));

        resources.disposeAll();

        assertEquals(List.of("third", "second", "first"), disposed);
    }

    @Test
    void disposeAllContinuesAfterResourceFailure() {
        ResourceManager resources = new ResourceManager();
        List<String> disposed = new ArrayList<>();

        resources.register(new TestResource("first", disposed));
        resources.register(new FailingResource("second", disposed));
        resources.register(new TestResource("third", disposed));

        resources.disposeAll();

        assertEquals(List.of("third", "second", "first"), disposed);
    }

    @Test
    void createShaderProgramReportsMissingProjectAssetAsAssetLoadException() {
        ResourceManager resources = new ResourceManager(new AssetConfig().setAssetRoot(assetRoot));

        AssetLoadException exception = assertThrows(
                AssetLoadException.class,
                () -> resources.createShaderProgram("missing")
        );

        assertTrue(exception.getMessage().contains("Failed to load asset:"));
        assertTrue(exception.getMessage().contains("missing.vert"));
    }

    @Test
    void createEngineShaderProgramReportsMissingBundledAssetAsAssetLoadException() {
        ResourceManager resources = new ResourceManager(new AssetConfig().setAssetRoot(assetRoot));

        AssetLoadException exception = assertThrows(
                AssetLoadException.class,
                () -> resources.createEngineShaderProgram("missing")
        );

        assertEquals("Failed to load engine asset: engine-assets/shaders/missing.vert", exception.getMessage());
    }

    @Test
    void createShaderProgramRejectsNullOrBlankNames() {
        ResourceManager resources = new ResourceManager();

        assertThrows(IllegalArgumentException.class, () -> resources.createShaderProgram(null));
        assertThrows(IllegalArgumentException.class, () -> resources.createShaderProgram(""));
        assertThrows(IllegalArgumentException.class, () -> resources.createShaderProgram(" "));
        assertThrows(IllegalArgumentException.class, () -> resources.createShaderProgram("valid", null));
        assertThrows(IllegalArgumentException.class, () -> resources.createShaderProgram(null, "valid"));
    }

    @Test
    void createTextureRejectsNullOrBlankPath() {
        ResourceManager resources = new ResourceManager();

        assertThrows(IllegalArgumentException.class, () -> resources.createTexture(null));
        assertThrows(IllegalArgumentException.class, () -> resources.createTexture(""));
        assertThrows(IllegalArgumentException.class, () -> resources.createTexture(" "));
    }

    @Test
    void createHDRTextureRejectsNullOrBlankPath() {
        ResourceManager resources = new ResourceManager();

        assertThrows(IllegalArgumentException.class, () -> resources.createHDRTexture(null));
        assertThrows(IllegalArgumentException.class, () -> resources.createHDRTexture(""));
        assertThrows(IllegalArgumentException.class, () -> resources.createHDRTexture(" "));
    }

    @Test
    void loadModelRejectsNullOrBlankPath() {
        ResourceManager resources = new ResourceManager();

        assertThrows(IllegalArgumentException.class, () -> resources.loadModel(null, new ModelLoadOptions()));
        assertThrows(IllegalArgumentException.class, () -> resources.loadModel("", new ModelLoadOptions()));
        assertThrows(IllegalArgumentException.class, () -> resources.loadModel(" ", new ModelLoadOptions()));
    }

    private static class TestResource implements Disposable {
        private final String name;
        private final List<String> disposed;

        private TestResource(String name, List<String> disposed) {
            this.name = name;
            this.disposed = disposed;
        }

        @Override
        public void dispose() {
            disposed.add(name);
        }
    }

    private static final class FailingResource extends TestResource {
        private FailingResource(String name, List<String> disposed) {
            super(name, disposed);
        }

        @Override
        public void dispose() {
            super.dispose();
            throw new IllegalStateException("dispose failed");
        }
    }
}
