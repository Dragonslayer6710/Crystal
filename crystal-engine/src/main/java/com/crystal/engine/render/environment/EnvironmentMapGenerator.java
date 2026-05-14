package com.crystal.engine.render.environment;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.graphics.TextureSettings;
import com.crystal.engine.render.gl.Framebuffer;
import com.crystal.engine.render.gl.RenderPass;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.texture.TextureFactory;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL46.*;

public final class EnvironmentMapGenerator implements Disposable {

    private static final int DEFAULT_CUBEMAP_SIZE = 512;

    private final Shader equirectangularToCubemapShader;
    private final Shader irradianceConvolutionShader;
    private final Shader prefilterEnvironmentShader;
    private final Shader brdfLutShader;
    private final Mesh cube;
    private final Mesh fullscreenQuad;

    private final Framebuffer framebuffer;

    private boolean disposed;

    public EnvironmentMapGenerator(Shader equirectangularToCubemapShader,
                                   Shader irradianceConvolutionShader,
                                   Shader prefilterEnvironmentShader,
                                   Shader brdfLutShader, Mesh cube, Mesh fullscreenQuad) {
        if (equirectangularToCubemapShader == null)
            throw new IllegalArgumentException("Equirectangular conversion shader cannot be null");

        if (irradianceConvolutionShader == null)
            throw new IllegalArgumentException("Irradiance convolution shader cannot be null");

        if (prefilterEnvironmentShader == null)
            throw new IllegalArgumentException("Prefilter environment shader cannot be null");

        if (brdfLutShader == null)
            throw new IllegalArgumentException("BRDF LUT shader cannot be null");

        if (cube == null)
            throw new IllegalArgumentException("Cube mesh cannot be null");

        if (fullscreenQuad == null)
            throw new IllegalArgumentException("Fullscreen quad mesh cannot be null");

        this.equirectangularToCubemapShader = equirectangularToCubemapShader;
        this.irradianceConvolutionShader = irradianceConvolutionShader;
        this.prefilterEnvironmentShader = prefilterEnvironmentShader;
        this.brdfLutShader = brdfLutShader;

        this.cube = cube;
        this.fullscreenQuad = fullscreenQuad;

        this.framebuffer = new Framebuffer("environment-map-generator");
        this.framebuffer.resizeDepthBuffer(DEFAULT_CUBEMAP_SIZE, DEFAULT_CUBEMAP_SIZE);
    }

    public Texture generateCubemap(Texture equirectangularTexture, int size) {
        if (equirectangularTexture == null)
            throw new IllegalArgumentException("Equirectangular texture cannot be null");

        return renderToCubemap(
                "<generated:environment-cubemap>",
                size,
                TextureSettings.defaultHDR(),
                equirectangularToCubemapShader,
                equirectangularTexture,
                "equirectangularMap",
                cube
        );
    }

    public Texture generateCubemap(Texture equirectangularTexture) {
        return generateCubemap(equirectangularTexture, DEFAULT_CUBEMAP_SIZE);
    }

    public Texture generateIrradianceMap(Texture environmentCubemap) {
        if (environmentCubemap == null)
            throw new IllegalArgumentException("Environment cubemap cannot be null");

        return renderToCubemap(
                "<generated:irradiance-map>",
                32,
                TextureSettings.defaultHDR(),
                irradianceConvolutionShader,
                environmentCubemap,
                "environmentMap",
                cube
        );
    }

    public Texture generatePrefilterMap(Texture environmentCubemap) {
        if (environmentCubemap == null)
            throw new IllegalArgumentException("Environment cubemap cannot be null");

        int size = 128;

        try (RenderPass pass = new RenderPass(framebuffer, size, size)) {
            Texture output = TextureFactory.createCubemap(
                    size,
                    TextureSettings.defaultPrefilterCubemap(),
                    "<generated:prefilter-map>"
            );

            Matrix4f projection = createCaptureProjection();
            Matrix4f[] views = createCaptureViews();

            prefilterEnvironmentShader.bind();
            prefilterEnvironmentShader.setInt("environmentMap", 0);
            prefilterEnvironmentShader.setMat4("projection", projection);

            environmentCubemap.bind(0);

            glDisable(GL_CULL_FACE);

            final int maxMipLevels = output.getMipLevels();

            for (int mip = 0; mip < maxMipLevels; mip++) {
                int mipWidth = Math.max(1, size >> mip);
                int mipHeight = Math.max(1, size >> mip);

                framebuffer.attachDepthBuffer();
                framebuffer.resizeDepthBuffer(mipWidth, mipHeight);

                glViewport(0, 0, mipWidth, mipHeight);

                float roughness = (float) mip / (float) (maxMipLevels - 1);

                prefilterEnvironmentShader.setFloat("roughness", roughness);

                for (int face = 0; face < 6; face++) {
                    prefilterEnvironmentShader.setMat4("view", views[face]);

                    framebuffer.attachCubemapFace(output, mip, face);

                    pass.clearColorDepth();
                    drawMesh(cube);
                }
            }

            return output;
        }
    }

    public Texture generateBrdfLut() {
        int size = 512;

        try (RenderPass pass = new RenderPass(framebuffer, size, size)) {
            Texture output = TextureFactory.createRenderTexture2D(
                    size,
                    size,
                    TextureSettings.defaultBrdfLut(),
                    "<generated:brdf-lut>"
            );

            framebuffer.detachDepthBuffer();
            framebuffer.attachTexture2D(output, 0);

            glDisable(GL_DEPTH_TEST);
            glDisable(GL_CULL_FACE);

            brdfLutShader.bind();

            pass.clearColor();
            drawMesh(fullscreenQuad);

            return output;
        }
    }

    private Texture renderToCubemap(String debugname, int size, TextureSettings settings, Shader shader,
                                    Texture inputTexture, String inputSamplerName, Mesh mesh) {

        try (RenderPass pass = new RenderPass(framebuffer, size, size)) {
            Texture output = TextureFactory.createCubemap(
                    size,
                    settings,
                    debugname
            );

            framebuffer.attachDepthBuffer();
            framebuffer.resizeDepthBuffer(size, size);

            Matrix4f projection = createCaptureProjection();
            Matrix4f[] views = createCaptureViews();

            shader.bind();
            shader.setInt(inputSamplerName, 0);
            shader.setMat4("projection", projection);

            inputTexture.bind(0);

            glDisable(GL_CULL_FACE);

            for (int face = 0; face < 6; face++) {
                shader.setMat4("view", views[face]);

                framebuffer.attachCubemapFace(output, 0, face);

                pass.clearColorDepth();
                drawMesh(mesh);
            }

            if (output.hasMipmaps())
                glGenerateTextureMipmap(output.getId());


            return output;
        }
    }

    private static Matrix4f createCaptureProjection() {
        return new Matrix4f().perspective((float) Math.toRadians(90.0f), 1.0f, 0.1f, 10.0f);
    }

    private static Matrix4f[] createCaptureViews() {
        return new Matrix4f[] {
                new Matrix4f().lookAt(0, 0, 0,  1,  0,  0,  0, -1,  0),
                new Matrix4f().lookAt(0, 0, 0, -1,  0,  0,  0, -1,  0),
                new Matrix4f().lookAt(0, 0, 0,  0,  1,  0,  0,  0,  1),
                new Matrix4f().lookAt(0, 0, 0,  0, -1,  0,  0,  0, -1),
                new Matrix4f().lookAt(0, 0, 0,  0,  0,  1,  0, -1,  0),
                new Matrix4f().lookAt(0, 0, 0,  0,  0, -1,  0, -1,  0)
        };
    }

    private void drawMesh(Mesh mesh) {
        mesh.bind();

        if (mesh.isIndexed()) {
            glDrawElements(
                    mesh.getPrimTypeValue(),
                    mesh.getIndexCount(),
                    GL_UNSIGNED_INT,
                    0
            );
        } else {
            glDrawArrays(
                    mesh.getPrimTypeValue(),
                    0,
                    mesh.getVertexCount()
            );
        }
    }

    @Override
    public void dispose() {
        if (disposed) return;

        framebuffer.dispose();

        disposed = true;
    }
}
