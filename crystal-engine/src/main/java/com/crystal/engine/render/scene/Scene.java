package com.crystal.engine.render.scene;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private final List<SceneObject> sceneObjects = new ArrayList<>();
    private final Camera camera = new Camera(0, 0, 0);
    private final DirectionalLight directionalLight = new DirectionalLight();

    public void add(SceneObject sceneObject) {
        sceneObjects.add(sceneObject);
    }

    public List<SceneObject> getRenderables() {
        return sceneObjects;
    }

    public Camera getCamera() {
        return camera;
    }

    public DirectionalLight getDirectionalLight() {
        return directionalLight;
    }
}
