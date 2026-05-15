package com.crystal.engine.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ResourceManagerTest {

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
