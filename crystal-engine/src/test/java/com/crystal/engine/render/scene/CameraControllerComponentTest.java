package com.crystal.engine.render.scene;

import com.crystal.engine.input.Input;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CameraControllerComponentTest {

    @Test
    void constructorRejectsNullCamera() {
        assertThrows(IllegalArgumentException.class, () -> new TestCameraController(null));
    }

    @Test
    void updatePassesCameraAndContextToController() {
        Camera camera = new Camera(0.0f, 0.0f, 0.0f);
        SceneUpdateContext context = context();
        TestCameraController controller = new TestCameraController(camera);

        controller.update(context);

        assertSame(camera, controller.updatedCamera);
        assertSame(context, controller.updatedContext);
    }

    @Test
    void updateRejectsNullContext() {
        TestCameraController controller = new TestCameraController(new Camera(0.0f, 0.0f, 0.0f));

        assertThrows(IllegalArgumentException.class, () -> controller.update(null));
    }

    private static SceneUpdateContext context() {
        return new SceneUpdateContext(0.25, new Input(), new Window(new WindowConfig()));
    }

    private static final class TestCameraController extends CameraControllerComponent {
        private Camera updatedCamera;
        private SceneUpdateContext updatedContext;

        private TestCameraController(Camera camera) {
            super(camera);
        }

        @Override
        protected void updateCamera(Camera camera, SceneUpdateContext context) {
            updatedCamera = camera;
            updatedContext = context;
        }
    }
}
