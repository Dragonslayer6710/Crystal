package com.crystal.engine.render.scene;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private final List<SceneObject> rootObjects = new ArrayList<>();
    private final Camera camera = new Camera(0, 0, 0);
    private final DirectionalLight directionalLight = new DirectionalLight();
    private final Vector3f ambientColor = new Vector3f(1.0f);
    private float ambientIntensity = 0.2f;

    public void add(SceneObject object) {
        if (object.getParent() != null) {
            throw new IllegalArgumentException("Only root objects can be added to Scene");
        }

        rootObjects.add(object);
    }

    public List<SceneObject> getRootObjects() {
        return rootObjects;
    }

    public Camera getCamera() {
        return camera;
    }

    public DirectionalLight getDirectionalLight() {
        return directionalLight;
    }

    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    public float getAmbientIntensity() {
        return ambientIntensity;
    }

    public Scene setAmbientColor(float r, float g, float b) {
        ambientColor.set(r, g, b);
        return this;
    }

    public Scene setAmbientIntensity(float ambientIntensity) {
        this.ambientIntensity = ambientIntensity;
        return this;
    }
}
