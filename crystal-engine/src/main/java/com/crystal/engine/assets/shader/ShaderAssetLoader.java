package com.crystal.engine.assets.shader;

import com.crystal.engine.assets.AssetResolver;
import com.crystal.engine.render.shader.Shader;

public final class ShaderAssetLoader {

    private final AssetResolver assets;

    public ShaderAssetLoader(AssetResolver assets) {
        if (assets == null) throw new IllegalArgumentException("AssetResolver cannot be null");
        this.assets = assets;
    }

    public Shader loadProjectShader(String vName, String fName) {
        String vertexPath = assets.projectShaderPath(vName, "vert");
        String fragmentPath = assets.projectShaderPath(fName, "frag");

        String vertexSource = assets.loadProjectAssetAsString(vertexPath);
        String fragmentSource = assets.loadProjectAssetAsString(fragmentPath);

        Shader shader = new Shader(vertexSource, fragmentSource, vertexPath, fragmentPath);
        shader.setDebugLabel(vertexPath + "|" + fragmentPath);

        return shader;
    }

    public Shader loadProjectShader(String name) {
        return loadProjectShader(name, name);
    }

    public Shader loadEngineShader(String vName, String fName) {
        String vertexPath = assets.engineShaderPath(vName, "vert");
        String fragmentPath = assets.engineShaderPath(fName, "frag");

        String vertexSource = assets.loadEngineAssetAsString(vertexPath);
        String fragmentSource = assets.loadEngineAssetAsString(fragmentPath);

        Shader shader = new Shader(vertexSource, fragmentSource, vertexPath, fragmentPath);
        shader.setDebugLabel(vertexPath + "|" + fragmentPath);

        return shader;
    }

    public Shader loadEngineShader(String name) {
        return loadEngineShader(name, name);
    }

    public String projectShaderCacheKey(String vName, String fName) {
        return assets.projectShaderPath(vName, "vert")
            + "|"
            + assets.projectShaderPath(fName, "frag");
    }

    public String engineShaderCacheKey(String vName, String fName) {
        return assets.engineShaderPath(vName, "vert")
            + "|"
            + assets.engineShaderPath(fName, "frag");
    }
}
