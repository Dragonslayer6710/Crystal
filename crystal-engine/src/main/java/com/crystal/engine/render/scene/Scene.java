package com.crystal.engine.render.scene;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.render.environment.Environment;
import com.crystal.engine.render.gl.UniformBuffer;
import com.crystal.engine.render.uniform.SceneUniformData;

import java.util.ArrayList;
import java.util.List;

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

    public Environment getEnvironment() {
        return environment;
    }

    public UniformBuffer getSceneUBO() {
        return sceneUBO;
    }

    @Override
    public void dispose() {
        sceneUBO.dispose();
    }
}
