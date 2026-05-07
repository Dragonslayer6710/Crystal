package com.crystal.engine.core;

import com.crystal.engine.graphics.PrimitiveType;
import com.crystal.engine.graphics.TextureSettings;
import com.crystal.engine.graphics.TextureType;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.VertexLayout;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.texture.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

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

    public Shader createShaderProgram(String vName, String fName) {
        String vs = loadAssetAsString("shaders/" + vName + ".vert");
        String fs = loadAssetAsString("shaders/" + fName + ".frag");
        return register(new Shader(vs, fs));
    }

    public Shader createShaderProgram(String name) {
        return  createShaderProgram(name, name);
    }

    public Texture createTexture(String path, TextureSettings settings) {
        return register(TextureLoader.load(
                assetRoot.resolve("textures/" + path),
                settings
        ));
    }

    public Texture createTexture(String path, TextureType type) {
        return createTexture(path, TextureSettings.forType(type));
    }

    public Texture createDataTexture(String path) {
        return createTexture(path, TextureSettings.defaultData());
    }

    public Texture createTexture(String path) {
        return createTexture(path, TextureSettings.defaultAlbedo());
    }

    public void disposeAll() {
        for (Disposable r : resources) {
            try {
                r.dispose();
            } catch (Exception e) {
                logger.error(e.getMessage());
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
