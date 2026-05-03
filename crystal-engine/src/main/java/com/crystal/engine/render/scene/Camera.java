package com.crystal.engine.render.scene;

public class Camera {
    private final Transform transform;

    public Camera(float xPos, float yPos, float zPos) {
        transform = new Transform(xPos, yPos, zPos);
    }

    public Transform getTransform() {
        return transform;
    }
}
