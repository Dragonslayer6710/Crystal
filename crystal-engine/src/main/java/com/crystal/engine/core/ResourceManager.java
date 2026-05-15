package com.crystal.engine.core;

import com.crystal.engine.assets.model.AssimpModelLoader;
import com.crystal.engine.assets.model.Model;
import com.crystal.engine.assets.model.ModelLoadOptions;
import com.crystal.engine.core.exception.AssetLoadException;
import com.crystal.engine.graphics.PrimitiveType;
import com.crystal.engine.graphics.TextureSettings;
import com.crystal.engine.graphics.TextureType;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.MeshData;
import com.crystal.engine.render.mesh.MeshFactory;
import com.crystal.engine.render.mesh.VertexLayout;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.texture.TextureFactory;
import com.crystal.engine.render.texture.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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
    private static final String ENGINE_ASSET_ROOT = "engine-assets";

    private final List<Disposable> resources = new ArrayList<>();
    private final Path assetRoot;

    private Texture defaultWhiteTexture;
    private Texture defaultNormalTexture;
    private Texture defaultBlackCubemap;
    private Texture defaultBrdfLut;

    private Shader skyboxShader;
    private Mesh skyboxCubeMesh;

    private final Map<String, Texture> textureCache = new HashMap<>();
    private final Map<String, Shader> shaderCache = new HashMap<>();

    private boolean disposed;

    public ResourceManager(AssetConfig config) {
        if (config == null) throw new IllegalArgumentException("AssetConfig cannot be null");
        this.assetRoot = config.getAssetRoot().toAbsolutePath().normalize();
    }

    public ResourceManager() {
        this(new AssetConfig());
    }

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

    public Mesh createMesh(MeshData data) {
        if (data == null) throw new IllegalArgumentException("MeshData cannot be null");

        return createMesh(
                PrimitiveType.TRIANGLES,
                data.vertices(),
                data.indices(),
                data.layout()
        );
    }

    public Shader createShaderProgram(String vName, String fName) {
        String vertexPath = projectShaderPath(vName, "vert");
        String fragmentPath = projectShaderPath(fName, "frag");
        String cacheKey = shaderCacheKey(vertexPath, fragmentPath);

        return shaderCache.computeIfAbsent(cacheKey, ignored -> {
            String vs = loadAssetAsString(vertexPath);
            String fs = loadAssetAsString(fragmentPath);

            Shader shader = new Shader(vs, fs, vertexPath, fragmentPath);
            shader.setDebugLabel(cacheKey);

            return register(shader);
        });
    }

    public Shader createShaderProgram(String name) {
        return createShaderProgram(name, name);
    }

    public Shader createEngineShaderProgram(String vName, String fName) {
        String vertexPath = engineShaderPath(vName, "vert");
        String fragmentPath = engineShaderPath(fName, "frag");
        String cacheKey = shaderCacheKey(vertexPath, fragmentPath);

        return shaderCache.computeIfAbsent(cacheKey, ignored -> {
            String vs = loadEngineAssetAsString(vertexPath);
            String fs = loadEngineAssetAsString(fragmentPath);

            Shader shader = new Shader(vs, fs, vertexPath, fragmentPath);
            shader.setDebugLabel(cacheKey);

            return register(shader);
        });
    }

    public Shader createEngineShaderProgram(String name) {
        return createEngineShaderProgram(name, name);
    }

    public Texture createTexture(String path, TextureSettings settings) {
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        String texturePath = texturePath(path);
        String cacheKey = textureCacheKey(texturePath, settings);

        return textureCache.computeIfAbsent(cacheKey, ignored -> register(TextureLoader.load(
                assetRoot.resolve(texturePath), settings)
        ));
    }

    public Texture createTexture(String path, TextureType type) {
        return createTexture(path, TextureSettings.forType(type));
    }

    public Texture createTexture(String path) {
        return createTexture(path, TextureSettings.defaultAlbedo());
    }

    public Texture createDataTexture(String path) {
        return createTexture(path, TextureSettings.defaultData());
    }

    public Texture createHDRTexture(String path) {
        TextureSettings settings = TextureSettings.defaultHDR();
        String texturePath = texturePath(path);
        String cacheKey = textureCacheKey(texturePath, settings);

        return textureCache.computeIfAbsent(cacheKey, ignored -> register(TextureLoader.loadHDR(
                assetRoot.resolve(texturePath),
                settings
        )));
    }

    public Texture getDefaultWhiteTexture() {
        if (defaultWhiteTexture == null) {
            defaultWhiteTexture = register(TextureFactory.create1x1(
                    "default-white", 255, 255, 255, 255
            ));
        }

        return defaultWhiteTexture;
    }

    public Texture getDefaultNormalTexture() {
        if (defaultNormalTexture == null) {
            defaultNormalTexture = register(TextureFactory.create1x1(
                    "default-normal", 128, 128, 255, 255
            ));
        }

        return defaultNormalTexture;
    }

    public Texture getDefaultBlackCubemap() {
        if (defaultBlackCubemap == null) {
            defaultBlackCubemap = register(TextureFactory.createCubemap(
                    1,
                    "default-black-cubemap"
            ));
        }

        return defaultBlackCubemap;
    }

    public Texture getDefaultBrdfLut() {
        if (defaultBrdfLut == null) {
            defaultBrdfLut = register(TextureFactory.create1x1(
                    "default-brdf-lut", 255, 255, 255, 255
            ));
        }

        return defaultBrdfLut;
    }

    public Shader getSkyboxShader() {
        if (skyboxShader == null) {
            skyboxShader = createEngineShaderProgram("skybox");
        }

        return skyboxShader;
    }

    public Mesh getSkyboxCubeMesh() {
        if (skyboxCubeMesh == null) {
            skyboxCubeMesh = MeshFactory.createPositionOnlyCube(this);
        }

        return skyboxCubeMesh;
    }

    public void disposeAll() {
        if (disposed) return;
        disposed = true;

        for (int i = resources.size() - 1; i >= 0; i--) {
            Disposable resource = resources.get(i);

            try {
                resource.dispose();
            } catch (Exception e) {
                logger.warn("Failed to dispose resource {}", resource.getClass().getName(), e);
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
            throw new AssetLoadException("Failed to load asset: " + fullPath.toAbsolutePath(), e);
        }
    }

    private String loadEngineAssetAsString(String path) {
        try (InputStream stream = ResourceManager.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null)
                throw new AssetLoadException("Failed to load engine asset: " + path);

            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssetLoadException("Failed to load engine asset: " + path, e);
        }
    }

    public Model loadModel(String path, ModelLoadOptions options) {
        return AssimpModelLoader.load(modelPath(path), this, options);
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

    private String projectShaderPath(String name, String extension) {
        return "shaders/" + name + "." + extension;
    }

    private String engineShaderPath(String name, String extension) {
        return ENGINE_ASSET_ROOT + "/shaders/" + name + "." + extension;
    }

    private String shaderCacheKey(String vertexPath, String fragmentPath) {
        return vertexPath + "|" + fragmentPath;
    }

    private String texturePath(String path) {
        return "textures/" + path;
    }

    private String textureCacheKey(String path, TextureSettings settings) {
        return path + "|" + settings.cacheKey();
    }

    private Path modelPath(String path) {
        return assetRoot.resolve("models/" + path);
    }
}
