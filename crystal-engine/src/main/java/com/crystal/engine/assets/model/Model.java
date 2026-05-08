package com.crystal.engine.assets.model;

import com.crystal.engine.render.scene.SceneObject;

import java.util.ArrayList;
import java.util.List;

public final class Model {

    private final List<SceneObject> rootObjects = new ArrayList<>();

    public void addRootObject(SceneObject object) {
        if (object == null) throw new IllegalArgumentException("SceneObject cannot be null");

        rootObjects.add(object);
    }

    public List<SceneObject> getRootObjects() {
        return rootObjects;
    }
}
