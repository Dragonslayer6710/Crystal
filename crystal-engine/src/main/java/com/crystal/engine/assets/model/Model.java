package com.crystal.engine.assets.model;

import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.scene.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class Model {

    private static final Logger logger = LoggerFactory.getLogger(Model.class);

    private final List<SceneObject> rootObjects = new ArrayList<>();

    public void addRootObject(SceneObject object) {
        if (object == null) throw new IllegalArgumentException("SceneObject cannot be null");

        rootObjects.add(object);
    }

    public List<SceneObject> getRootObjects() {
        return rootObjects;
    }

    public void logHierarchy() {
        StringBuilder builder = new StringBuilder();

        builder.append("Model hierarchy:\n");

        for (SceneObject root : rootObjects)
            appendObject(builder, root, 0);

        logger.info("\n{}", builder);
    }

    public SceneObject instantiate() {
        if (rootObjects.isEmpty()) throw new IllegalStateException("Cannot instantiate model with no root objects");

        if (rootObjects.size() == 1)
            return rootObjects.getFirst().copyHierarchy();

        SceneObject root = new SceneObject("Model", null, null, new Transform());

        for (SceneObject object: rootObjects)
            root.addChild(object.copyHierarchy());

        return root;
    }

    private void appendObject(StringBuilder builder, SceneObject object, int depth) {
        builder.append("  ".repeat(depth))
                .append("- ")
                .append(object.getName())
                .append(" ")
                .append(object.isRenderable() ? "[mesh]" : "[node]")
                .append("\n");

        for (SceneObject child : object.getChildren())
            appendObject(builder, child, depth + 1);
    }
}
