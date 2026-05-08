package com.crystal.engine.core;

import com.crystal.engine.assets.model.AssimpModelLoader;
import com.crystal.engine.assets.model.Model;
import com.crystal.engine.assets.model.ModelLoadOptions;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    private final List<Disposable> resources = new ArrayList<>();
    private final Path assetRoot = Path.of("assets");

    private final Map<String, Texture> textureCache = new HashMap<>();
    private final Map<String, Shader> shaderCache = new HashMap<>();

    private boolean disposed;

    public <T extends Disposable> T register(T resource) {
        if (disposed) throw new IllegalStateException("ResourceManager has already been disposed");

        if (resource == null)
            throw new IllegalArgumentException("Resource cannot be null");


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
        String cacheKey = "shaders/" + vName + ".vert|shaders/" + fName + ".frag";

        return shaderCache.computeIfAbsent(cacheKey, key -> {
            String[] paths = cacheKey.split("\\|");

            String vs = loadAssetAsString(paths[0]);
            String fs = loadAssetAsString(paths[1]);

            Shader shader = new Shader(vs, fs, paths[0], paths[1]);
            shader.setDebugLabel(cacheKey);

            return register(shader);
        });
    }

    public Shader createShaderProgram(String name) {
        return  createShaderProgram(name, name);
    }

    public Texture createTexture(String path, TextureSettings settings) {
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        String cacheKey = "textures/" + path + "|" + settings.cacheKey();

        return textureCache.computeIfAbsent(cacheKey, key -> register(TextureLoader.load(
                assetRoot.resolve("textures/" + path), settings)
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
        if (disposed) return;
        disposed = true;

        for (Disposable r : resources) {
            try {
                r.dispose();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        resources.clear();
        textureCache.clear();
        shaderCache.clear();
    }

    private String loadAssetAsString(String path) {
        Path fullPath = assetRoot.resolve(path);

        try {
            return Files.readString(fullPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load asset: " + fullPath.toAbsolutePath(), e);
        }
    }

    public Model loadModel(String path, ModelLoadOptions options) {
        return AssimpModelLoader.load(assetRoot.resolve("models/" + path), this, options);
    }

    public Texture loadTexture(Path path, TextureSettings settings) {
        if (path == null) throw new IllegalArgumentException("Path cannot be null");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        String cacheKey = path.toAbsolutePath().normalize() + "|" + settings.cacheKey();

        return textureCache.computeIfAbsent(cacheKey, key ->
                register(TextureLoader.load(path, settings))
        );
    }

    public Texture loadEmbeddedTexture(String key, ByteBuffer encodedImage, TextureSettings settings) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Path cannot be null or blank");
        if (encodedImage == null) throw new IllegalArgumentException("Encoded image cannot be null");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        String cacheKey = "embedded:" + key + "|" + settings.cacheKey();

        return textureCache.computeIfAbsent(cacheKey, ignored ->
                register(TextureLoader.loadFromMemory(encodedImage, settings, cacheKey))
        );
    }
}
