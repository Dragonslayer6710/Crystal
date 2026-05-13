package com.crystal.engine.render.environment;

import com.crystal.engine.core.ResourceManager;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.MeshFactory;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.texture.Texture;

public final class IBLGenerator {

    private final ResourceManager resources;
    private final EnvironmentMapGenerator generator;

    private int environmentMapSize = 512;

    public static IBLGenerator createDefault(ResourceManager resources) {
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");

        Shader envShader = resources.createShaderProgram(
                "cubemap_capture",
                "equirectangular_to_cubemap"
        );

        Shader irradianceShader = resources.createShaderProgram(
                "cubemap_capture",
                "irradiance_convolution"
        );

        Shader prefilterShader = resources.createShaderProgram(
                "cubemap_capture",
                "prefilter_environment"
        );

        Shader brdfLutShader = resources.createShaderProgram("brdf_lut");

        Mesh envCube = MeshFactory.createPositionOnlyCube(resources);
        Mesh fullscreenQuad = MeshFactory.createFullscreenQuad(resources);

        EnvironmentMapGenerator generator = resources.register(new EnvironmentMapGenerator(
                envShader,
                irradianceShader,
                prefilterShader,
                brdfLutShader,
                envCube,
                fullscreenQuad
        ));

        return new IBLGenerator(resources, generator);
    }

    public IBLGenerator(ResourceManager resources, EnvironmentMapGenerator generator) {
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");
        if (generator == null)
            throw new IllegalArgumentException("EnvironmentMapGenerator cannot be null");

        this.resources = resources;
        this.generator = generator;
    }

    public IBLGenerator setEnvironmentMapSize(int environmentMapSize) {
        if (environmentMapSize <= 0) throw new IllegalArgumentException("Environment map size must be greater than 0");

        this.environmentMapSize = environmentMapSize;
        return this;
    }

    public void generate(Environment environment, Texture hdrTexture) {
        if (environment == null) throw new IllegalArgumentException("Environment cannot be null");
        if (hdrTexture == null) throw new IllegalArgumentException("HDR texture cannot be null");

        Texture environmentCubemap = resources.register(generator.generateCubemap(hdrTexture, environmentMapSize));
        Texture irradianceMap = resources.register(generator.generateIrradianceMap(environmentCubemap));
        Texture prefilterMap = resources.register(generator.generatePrefilterMap(environmentCubemap));
        Texture brdfLut = resources.register(generator.generateBrdfLut());

        environment.setSkybox(environmentCubemap)
                .setIrradianceMap(irradianceMap)
                .setPrefilterMap(prefilterMap)
                .setBrdfLut(brdfLut);
    }

    public void generateFromHDR(Environment environment, String hdrTexturePath) {
        if (hdrTexturePath == null || hdrTexturePath.isBlank())
            throw new IllegalArgumentException("HDR texture path cannot be null or blank");

        Texture hdrTexture = resources.createHDRTexture(hdrTexturePath);

        generate(environment, hdrTexture);
    }
}
