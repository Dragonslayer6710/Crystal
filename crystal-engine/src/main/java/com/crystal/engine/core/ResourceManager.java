package com.crystal.engine.core;

import com.crystal.engine.render.api.PrimitiveType;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.shader.ShaderProgram;

import java.util.ArrayList;
import java.util.List;

public class ResourceManager {
    private final List<Disposable> resources = new ArrayList<>();

    public <T extends Disposable> T register(T resource) {
        resources.add(resource);
        return resource;
    }

    public Mesh createMesh(PrimitiveType type, float[] vertices) {
        return register(new Mesh(type, vertices));
    }

    public ShaderProgram createShaderProgram(String vs, String fs) {
        return register(new ShaderProgram(vs, fs));
    }

    public void disposeAll() {
        for (Disposable r : resources) {
            try {
                r.dispose();
            } catch (Exception e) {
                e.printStackTrace(); // replace with logger later
            }
        }
        resources.clear();
    }
}
