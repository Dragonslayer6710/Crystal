package com.crystal.engine.render.scene;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private final List<SceneObject> rootObjects = new ArrayList<>();
    private final Camera camera = new Camera(0, 0, 0);
    private final DirectionalLight directionalLight = new DirectionalLight();

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
}
