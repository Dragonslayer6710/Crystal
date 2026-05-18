package com.crystal.engine.core;

import com.crystal.engine.assets.model.AssimpModelLoader;
import com.crystal.engine.assets.model.Model;
import com.crystal.engine.assets.model.ModelLoadOptions;
import com.crystal.engine.graphics.PrimitiveType;
import com.crystal.engine.graphics.TextureSettings;
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

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    private final AssetResolver assets;
    private final List<Disposable> resources = new ArrayList<>();

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
        this.assets = new AssetResolver(config);
    }

    public ResourceManager() {
        this(new AssetConfig());
    }

    public <T extends Disposable> T manageResource(T resource) {
        if (disposed) throw new IllegalStateException("ResourceManager has already been disposed");

        if (resource == null)
            throw new IllegalArgumentException("Resource cannot be null");


        resources.add(resource);
        return resource;
    }

    public Mesh createMesh(PrimitiveType type, float[] vertices, int[] indices, VertexLayout layout) {
        return manageResource(new Mesh(type, vertices, indices, layout));
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
        String vertexPath = assets.projectShaderPath(vName, "vert");
        String fragmentPath = assets.projectShaderPath(fName, "frag");
        String cacheKey = shaderCacheKey(vertexPath, fragmentPath);

        return shaderCache.computeIfAbsent(cacheKey, ignored -> {
            String vs = assets.loadProjectAssetAsString(vertexPath);
            String fs = assets.loadProjectAssetAsString(fragmentPath);

            Shader shader = new Shader(vs, fs, vertexPath, fragmentPath);
            shader.setDebugLabel(cacheKey);

            return manageResource(shader);
        });
    }

    public Shader createShaderProgram(String name) {
        return createShaderProgram(name, name);
    }

    public Shader createEngineShaderProgram(String vName, String fName) {
        String vertexPath = assets.engineShaderPath(vName, "vert");
        String fragmentPath = assets.engineShaderPath(fName, "frag");
        String cacheKey = shaderCacheKey(vertexPath, fragmentPath);

        return shaderCache.computeIfAbsent(cacheKey, ignored -> {
            String vs = assets.loadEngineAssetAsString(vertexPath);
            String fs = assets.loadEngineAssetAsString(fragmentPath);

            Shader shader = new Shader(vs, fs, vertexPath, fragmentPath);
            shader.setDebugLabel(cacheKey);

            return manageResource(shader);
        });
    }

    public Shader createEngineShaderProgram(String name) {
        return createEngineShaderProgram(name, name);
    }

    public Texture createTexture(String path, TextureSettings settings) {
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        String texturePath = assets.projectTextureAssetPath(path);
        String cacheKey = textureCacheKey(texturePath, settings);

        return textureCache.computeIfAbsent(cacheKey, ignored -> manageResource(TextureLoader.load(
            assets.projectTexturePath(path),
            settings
        )));
    }

    public Texture createTexture(String path) {
        return createTexture(path, TextureSettings.defaultAlbedo());
    }

    public Texture createDataTexture(String path) {
        return createTexture(path, TextureSettings.defaultData());
    }

    public Texture createHDRTexture(String path) {
        TextureSettings settings = TextureSettings.defaultHDR();
        String texturePath = assets.projectTextureAssetPath(path);
        String cacheKey = textureCacheKey(texturePath, settings);

        return textureCache.computeIfAbsent(cacheKey, ignored -> manageResource(TextureLoader.loadHDR(
            assets.projectTexturePath(path),
            settings
        )));
    }

    public Texture getDefaultWhiteTexture() {
        if (defaultWhiteTexture == null) {
            defaultWhiteTexture = manageResource(TextureFactory.create1x1(
                    "default-white", 255, 255, 255, 255
            ));
        }

        return defaultWhiteTexture;
    }

    public Texture getDefaultNormalTexture() {
        if (defaultNormalTexture == null) {
            defaultNormalTexture = manageResource(TextureFactory.create1x1(
                    "default-normal", 128, 128, 255, 255
            ));
        }

        return defaultNormalTexture;
    }

    public Texture getDefaultBlackCubemap() {
        if (defaultBlackCubemap == null) {
            defaultBlackCubemap = manageResource(TextureFactory.createCubemap(
                    1,
                    "default-black-cubemap"
            ));
        }

        return defaultBlackCubemap;
    }

    public Texture getDefaultBrdfLut() {
        if (defaultBrdfLut == null) {
            defaultBrdfLut = manageResource(TextureFactory.create1x1(
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

    public Model loadModel(String path, ModelLoadOptions options) {
        return AssimpModelLoader.load(assets.projectModelPath(path), this, options);
    }

    public Texture loadTexture(Path path, TextureSettings settings) {
        if (path == null) throw new IllegalArgumentException("Path cannot be null");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        String cacheKey = path.toAbsolutePath().normalize() + "|" + settings.cacheKey();

        return textureCache.computeIfAbsent(cacheKey, key ->
                manageResource(TextureLoader.load(path, settings))
        );
    }

    public Texture loadEmbeddedTexture(String key, ByteBuffer encodedImage, TextureSettings settings) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Path cannot be null or blank");
        if (encodedImage == null) throw new IllegalArgumentException("Encoded image cannot be null");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        String cacheKey = "embedded:" + key + "|" + settings.cacheKey();

        return textureCache.computeIfAbsent(cacheKey, ignored ->
                manageResource(TextureLoader.loadFromMemory(encodedImage, settings, cacheKey))
        );
    }

    private String shaderCacheKey(String vertexPath, String fragmentPath) {
        return vertexPath + "|" + fragmentPath;
    }

    private String textureCacheKey(String path, TextureSettings settings) {
        return path + "|" + settings.cacheKey();
    }
}
