package com.crystal.engine.core;

import com.crystal.engine.render.api.PrimitiveType;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.VertexLayout;
import com.crystal.engine.render.shader.ShaderProgram;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ResourceManager {
    private final List<Disposable> resources = new ArrayList<>();
    private final Path assetRoot = Path.of("assets");

    public <T extends Disposable> T register(T resource) {
        resources.add(resource);
        return resource;
    }

    public Mesh createMesh(PrimitiveType type, float[] vertices, int[] indices, VertexLayout layout) {
        return register(new Mesh(type, vertices, indices, layout));
    }

    public Mesh createMesh(PrimitiveType type, float[] vertices, int[] indices) {
        return register(new Mesh(type, vertices, indices));
    }

    public Mesh createMesh(PrimitiveType type, float[] vertices) {
        return createMesh(type, vertices, null);
    }

    public ShaderProgram createShaderProgram(String vName, String fName) {
        String vs = loadAssetAsString("shaders/" + vName + ".vert");
        String fs = loadAssetAsString("shaders/" + fName + ".frag");
        return register(new ShaderProgram(vs, fs));
    }

    public ShaderProgram createShaderProgram(String name) {
        return  createShaderProgram(name, name);
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

    private String loadAssetAsString(String path) {
        Path fullPath = assetRoot.resolve(path);

        try {
            return Files.readString(fullPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load asset: " + fullPath.toAbsolutePath(), e);
        }
    }
}
