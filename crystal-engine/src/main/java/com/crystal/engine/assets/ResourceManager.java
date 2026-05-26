package com.crystal.engine.assets;

import com.crystal.engine.assets.model.assimp.AssimpModelLoader;
import com.crystal.engine.assets.model.Model;
import com.crystal.engine.assets.model.ModelLoadOptions;
import com.crystal.engine.assets.shader.ShaderAssetLoader;
import com.crystal.engine.assets.texture.TextureAssetLoader;
import com.crystal.engine.audio.SoundBuffer;
import com.crystal.engine.assets.sound.SoundAssetLoader;
import com.crystal.engine.core.Disposable;
import com.crystal.engine.render.mesh.PrimitiveType;
import com.crystal.engine.render.texture.TextureSettings;
import com.crystal.engine.render.environment.Environment;
import com.crystal.engine.render.environment.IBLGenerator;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.MeshData;
import com.crystal.engine.render.mesh.MeshFactory;
import com.crystal.engine.render.mesh.VertexLayout;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.texture.TextureFactory;
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
    private final TextureAssetLoader textureAssetLoader;
    private final ShaderAssetLoader shaderAssetLoader;
    private final List<Disposable> resources = new ArrayList<>();

    private Texture defaultWhiteTexture;
    private Texture defaultNormalTexture;
    private Texture defaultBlackCubemap;
    private Texture defaultBrdfLut;

    private Shader skyboxShader;

    private Mesh skyboxCubeMesh;
    private Mesh litTexturedCubeMesh;
    private Mesh litTexturedPlaneMesh;
    private Mesh fullscreenQuadMesh;

    private final Map<String, Texture> textureCache = new HashMap<>();
    private final Map<String, Shader> shaderCache = new HashMap<>();
    private final Map<String, Model> modelCache = new HashMap<>();
    private final Map<String, Environment> iblEnvironmentCache = new HashMap<>();
    private final Map<String, SoundBuffer> soundCache = new HashMap<>();

    private boolean disposed;

    public ResourceManager(AssetConfig config) {
        this.assets = new AssetResolver(config);
        this.textureAssetLoader = new TextureAssetLoader(assets);
        this.shaderAssetLoader = new ShaderAssetLoader(assets);
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
        String cacheKey = shaderAssetLoader.projectShaderCacheKey(vName, fName);

        return shaderCache.computeIfAbsent(cacheKey, ignored ->
            manageResource(shaderAssetLoader.loadProjectShader(vName, fName))
        );
    }

    public Shader createShaderProgram(String name) {
        return createShaderProgram(name, name);
    }

    public Shader createEngineShaderProgram(String vName, String fName) {
        String cacheKey = shaderAssetLoader.engineShaderCacheKey(vName, fName);

        return shaderCache.computeIfAbsent(cacheKey, ignored ->
            manageResource(shaderAssetLoader.loadEngineShader(vName, fName))
        );
    }

    public Shader createEngineShaderProgram(String name) {
        return createEngineShaderProgram(name, name);
    }

    public Texture createTexture(String path, TextureSettings settings) {
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        String cacheKey = textureAssetLoader.projectTextureCacheKey(path, settings);

        return textureCache.computeIfAbsent(cacheKey, ignored ->
            manageResource(textureAssetLoader.loadProjectTexture(path, settings))
        );
    }

    public Texture createTexture(String path) {
        return createTexture(path, TextureSettings.defaultAlbedo());
    }

    public Texture createDataTexture(String path) {
        return createTexture(path, TextureSettings.defaultData());
    }

    public Texture createHDRTexture(String path) {
        TextureSettings settings = TextureSettings.defaultHDR();
        String cacheKey = textureAssetLoader.projectTextureCacheKey(path, settings);

        return textureCache.computeIfAbsent(cacheKey, ignored ->
            manageResource(textureAssetLoader.loadProjectHDRTexture(path))
        );
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

    public Mesh getLitTexturedCubeMesh() {
        if (litTexturedCubeMesh == null) {
            litTexturedCubeMesh = MeshFactory.createLitTexturedCube(this);
        }

        return litTexturedCubeMesh;
    }

    public Mesh getLitTexturedPlaneMesh() {
        if (litTexturedPlaneMesh == null) {
            litTexturedPlaneMesh = MeshFactory.createLitTexturedPlane(this);
        }

        return litTexturedPlaneMesh;
    }

    public Mesh getFullscreenQuadMesh() {
        if (fullscreenQuadMesh == null) {
            fullscreenQuadMesh = MeshFactory.createFullscreenQuad(this);
        }

        return fullscreenQuadMesh;
    }

    public Model loadModel(String path, ModelLoadOptions options) {
        if (options == null) throw new IllegalArgumentException("ModelLoadOptions cannot be null");

        Path modelPath = assets.projectModelPath(path);
        String cacheKey = modelCacheKey(modelPath, options);

        return modelCache.computeIfAbsent(cacheKey, ignored ->
            AssimpModelLoader.load(modelPath, this, options)
        );
    }

    public Texture loadTexture(Path path, TextureSettings settings) {
        if (path == null) throw new IllegalArgumentException("Path cannot be null");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        String cacheKey = path.toAbsolutePath().normalize() + "|" + settings.cacheKey();

        return textureCache.computeIfAbsent(cacheKey, key ->
            manageResource(textureAssetLoader.loadPath(path, settings))
        );
    }

    public Texture loadEmbeddedTexture(String key, ByteBuffer encodedImage, TextureSettings settings) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Path cannot be null or blank");
        if (encodedImage == null) throw new IllegalArgumentException("Encoded image cannot be null");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        String cacheKey = "embedded:" + key + "|" + settings.cacheKey();

        return textureCache.computeIfAbsent(cacheKey, ignored ->
            manageResource(textureAssetLoader.loadEmbedded(cacheKey, encodedImage, settings))
        );
    }

    public Environment getOrCreateIBLEnvironment(String hdrTexturePath) {
        if (hdrTexturePath == null || hdrTexturePath.isBlank())
            throw new IllegalArgumentException("HDR texture path cannot be null or blank");

        return iblEnvironmentCache.computeIfAbsent(hdrTexturePath, ignored -> {
            Environment environment = new Environment();
            IBLGenerator.createDefault(this).generateFromHDR(environment, hdrTexturePath);
            return environment;
        });
    }

    public SoundBuffer loadSound(String path) {
        String soundPath = assets.projectSoundAssetPath(path);

        return soundCache.computeIfAbsent(soundPath, ignored ->
            manageResource(SoundAssetLoader.loadOgg(assets.projectSoundPath(path)))
        );
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
        modelCache.clear();
        iblEnvironmentCache.clear();
        soundCache.clear();
    }

    private String modelCacheKey(Path path, ModelLoadOptions options) {
        return path.toAbsolutePath().normalize()
                + "|shader=" + System.identityHashCode(options.getShader())
                + "|flipUVs=" + options.isFlipUVs();
    }
}
