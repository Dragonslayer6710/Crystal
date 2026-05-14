package com.crystal.engine.render;

import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.shadow.ShadowMap;
import com.crystal.engine.render.texture.Texture;

public final class RenderResources {

    private final Texture defaultWhiteTexture;
    private final Texture defaultNormalTexture;
    private final Texture defaultBlackCubemap;
    private final Texture defaultBrdfLut;

    private final Shader skyboxShader;
    private final Mesh skyboxCubeMesh;

    private final Shader shadowShader;
    private final ShadowMap directionalShadowMap;

    public RenderResources(
            Texture defaultWhiteTexture,
            Texture defaultNormalTexture,
            Texture defaultBlackCubemap,
            Texture defaultBrdfLut,
            Shader skyboxShader,
            Mesh skyboxCubeMesh,
            Shader shadowShader,
            ShadowMap directionalShadowMap
    ) {
        if (defaultWhiteTexture == null) throw new IllegalArgumentException("Default white texture cannot be null");
        if (defaultNormalTexture == null) throw new IllegalArgumentException("Default normal texture cannot be null");
        if (defaultBlackCubemap == null) throw new IllegalArgumentException("Default black cubemap cannot be null");
        if (defaultBrdfLut == null) throw new IllegalArgumentException("Default BRDF LUT cannot be null");

        if (skyboxShader == null) throw new IllegalArgumentException("Skybox shader cannot be null");
        if (skyboxCubeMesh == null) throw new IllegalArgumentException("Skybox cube mesh cannot be null");

        if (shadowShader == null) throw new IllegalArgumentException("Shadow shader cannot be null");
        if (directionalShadowMap == null) throw new IllegalArgumentException("Directional shadow map cannot be null");

        this.defaultWhiteTexture = defaultWhiteTexture;
        this.defaultNormalTexture = defaultNormalTexture;
        this.defaultBlackCubemap = defaultBlackCubemap;
        this.defaultBrdfLut = defaultBrdfLut;
        this.skyboxShader = skyboxShader;
        this.skyboxCubeMesh = skyboxCubeMesh;
        this.shadowShader = shadowShader;
        this.directionalShadowMap = directionalShadowMap;
    }

    public Texture getDefaultWhiteTexture() {
        return defaultWhiteTexture;
    }

    public Texture getDefaultNormalTexture() {
        return defaultNormalTexture;
    }

    public Texture getDefaultBlackCubemap() {
        return defaultBlackCubemap;
    }

    public Texture getDefaultBrdfLut() {
        return defaultBrdfLut;
    }

    public Shader getSkyboxShader() {
        return skyboxShader;
    }

    public Mesh getSkyboxCubeMesh() {
        return skyboxCubeMesh;
    }

    public Shader getShadowShader() {
        return shadowShader;
    }

    public ShadowMap getDirectionalShadowMap() {
        return directionalShadowMap;
    }
}