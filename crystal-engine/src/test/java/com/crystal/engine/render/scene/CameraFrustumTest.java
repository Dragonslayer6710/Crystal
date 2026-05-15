package com.crystal.engine.render.scene;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraFrustumTest {

    @Test
    void cameraSeesObjectInFrontAfterFrustumUpdate() {
        Camera camera = new Camera(0, 0, 0);

        camera.updateFrustum(16.0f / 9.0f);

        assertTrue(camera.canSee(new Vector3f(0, 0, -5), 1.0f));
    }

    @Test
    void cameraDoesNotSeeObjectBehindItAfterFrustumUpdate() {
        Camera camera = new Camera(0, 0, 0);

        camera.updateFrustum(16.0f / 9.0f);

        assertFalse(camera.canSee(new Vector3f(0, 0, 5), 1.0f));
    }

    @Test
    void forwardAndRightVectorsReflectYawRotation() {
        Camera camera = new Camera(0, 0, 0);
        camera.getTransform().setRotation(0, (float) Math.toRadians(90.0), 0);

        Vector3f forward = camera.getForwardXZ();
        Vector3f right = camera.getRightXZ();

        assertEquals(-1.0f, forward.x, 0.0001f);
        assertEquals(0.0f, forward.z, 0.0001f);
        assertEquals(0.0f, right.x, 0.0001f);
        assertEquals(-1.0f, right.z, 0.0001f);
    }
}
