package com.crystal.engine.render.scene;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.render.environment.Environment;
import com.crystal.engine.render.gl.UniformBuffer;
import com.crystal.engine.render.uniform.SceneUniformData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Scene implements Disposable {

    private final List<SceneObject> rootObjects = new ArrayList<>();
    private final Camera camera = new Camera(0, 0, 0);

    private final DirectionalLight directionalLight = new DirectionalLight();
    private final Environment environment = new Environment();

    private final UniformBuffer sceneUBO = new UniformBuffer(
            SceneUniformData.BINDING_POINT,
            SceneUniformData.BYTE_SIZE
    );

    public void add(SceneObject object) {
        if (object == null) throw new IllegalArgumentException("SceneObject cannot be null");
        if (object.getParent() != null) throw new IllegalArgumentException("Only root objects can be added to Scene");

        if (rootObjects.contains(object))
            return;

        rootObjects.add(object);
    }

    public boolean remove(SceneObject object) {
        if (object == null)
            return false;

        boolean removed = rootObjects.remove(object);

        if (removed)
            object.getTransform().setParent(null);

        return removed;
    }

    public List<SceneObject> getRootObjects() {
        return rootObjects;
    }

    public void update(SceneUpdateContext context) {
        for (SceneObject object : rootObjects)
            object.update(context);
    }

    public Camera getCamera() {
        return camera;
    }

    public DirectionalLight getDirectionalLight() {
        return directionalLight;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public UniformBuffer getSceneUBO() {
        return sceneUBO;
    }

    public Optional<SceneObject> findByName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be null or blank");

        for (SceneObject object : rootObjects) {
            Optional<SceneObject> match = findByName(object, name);

            if (match.isPresent())
                return match;
        }

        return Optional.empty();
    }

    private Optional<SceneObject> findByName(SceneObject object, String name) {
        if (name.equals(object.getName()))
            return Optional.of(object);

        for (SceneObject child : object.getChildren()) {
            Optional<SceneObject> match = findByName(child, name);

            if (match.isPresent())
                return match;
        }

        return Optional.empty();
    }

    public void clear() {
        for (SceneObject object : rootObjects)
            object.getTransform().setParent(null);

        rootObjects.clear();
    }

    @Override
    public void dispose() {
        sceneUBO.dispose();
    }
}
