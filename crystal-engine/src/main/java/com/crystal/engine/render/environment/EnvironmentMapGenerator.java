package com.crystal.engine.render.environment;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.graphics.TextureSettings;
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

    private final int framebuffer;
    private final int renderbuffer;

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

        framebuffer = glCreateFramebuffers();
        renderbuffer = glCreateRenderbuffers();

        glNamedRenderbufferStorage(
                renderbuffer,
                GL_DEPTH_COMPONENT24,
                DEFAULT_CUBEMAP_SIZE,
                DEFAULT_CUBEMAP_SIZE
        );

        glNamedFramebufferRenderbuffer(
                framebuffer,
                GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER,
                renderbuffer
        );
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

        Texture output = TextureFactory.createCubemap(
                128,
                TextureSettings.defaultPrefilterCubemap(),
                "<generated:prefilter-map>"
        );

        Matrix4f projection = createCaptureProjection();
        Matrix4f[] views = createCaptureViews();

        GLStateSnapshot state = new GLStateSnapshot();

        try {
            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

            prefilterEnvironmentShader.bind();
            prefilterEnvironmentShader.setInt("environmentMap", 0);
            prefilterEnvironmentShader.setMat4("projection", projection);

            environmentCubemap.bind(0);

            glDisable(GL_CULL_FACE);

            final int maxMipLevels = 5;

            for (int mip = 0; mip < maxMipLevels; mip++) {
                int mipWidth = 128 >> mip;
                int mipHeight = 128 >> mip;

                glNamedRenderbufferStorage(
                        renderbuffer,
                        GL_DEPTH_COMPONENT24,
                        mipWidth,
                        mipHeight
                );

                glViewport(0, 0, mipWidth, mipHeight);

                float roughness = (float) mip / (float) (maxMipLevels - 1);

                prefilterEnvironmentShader.setFloat("roughness", roughness);

                for (int face = 0; face < 6; face++) {
                    prefilterEnvironmentShader.setMat4("view", views[face]);

                    glNamedFramebufferTextureLayer(
                            framebuffer,
                            GL_COLOR_ATTACHMENT0,
                            output.getId(),
                            mip,
                            face
                    );

                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                    drawMesh(cube);
                }
            }

            return output;
        } finally {
            state.restore();
        }
    }

    public Texture generateBrdfLut() {
        GLStateSnapshot state = new GLStateSnapshot();

        try {
            int size = 512;

            Texture output = TextureFactory.createRenderTexture2D(
                    size,
                    size,
                    TextureSettings.defaultHDR(),
                    "<generated:brdf-lut>"
            );

            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

            glNamedFramebufferTexture(
                    framebuffer,
                    GL_COLOR_ATTACHMENT0,
                    output.getId(),
                    0
            );

            glViewport(0, 0, size, size);

            glDisable(GL_DEPTH_TEST);
            glDisable(GL_CULL_FACE);

            brdfLutShader.bind();

            glClear(GL_COLOR_BUFFER_BIT);
            drawMesh(fullscreenQuad);

            return output;
        } finally {
            state.restore();
        }
    }

    private Texture renderToCubemap(String debugname, int size, TextureSettings settings, Shader shader,
                                    Texture inputTexture, String inputSamplerName, Mesh mesh) {
        GLStateSnapshot state = new GLStateSnapshot();

        try {
            Texture output = TextureFactory.createCubemap(
                    size,
                    settings,
                    debugname
            );

            glNamedRenderbufferStorage(renderbuffer, GL_DEPTH_COMPONENT24, size, size);

            Matrix4f projection = createCaptureProjection();
            Matrix4f[] views = createCaptureViews();

            glViewport(0, 0, size, size);

            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

            shader.bind();
            shader.setInt(inputSamplerName, 0);
            shader.setMat4("projection", projection);

            inputTexture.bind(0);

            glDisable(GL_CULL_FACE);

            for (int face = 0; face < 6; face++) {
                shader.setMat4("view", views[face]);

                glNamedFramebufferTextureLayer(
                        framebuffer,
                        GL_COLOR_ATTACHMENT0,
                        output.getId(),
                        0,
                        face
                );

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                drawMesh(mesh);
            }

            if (output.hasMipmaps())
                glGenerateTextureMipmap(output.getId());


            return output;
        } finally {
            state.restore();
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

    private static final class GLStateSnapshot {
        final int viewportX;
        final int viewportY;
        final int viewportWidth;
        final int viewportHeight;
        final int framebuffer;
        final boolean depthTest;
        final boolean cullFace;

        GLStateSnapshot() {
            int[] viewport = new int[4];
            glGetIntegerv(GL_VIEWPORT, viewport);

            this.viewportX = viewport[0];
            this.viewportY = viewport[1];
            this.viewportWidth = viewport[2];
            this.viewportHeight = viewport[3];

            this.framebuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
            this.depthTest = glIsEnabled(GL_DEPTH_TEST);
            this.cullFace = glIsEnabled(GL_CULL_FACE);
        }

        void restore() {
            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
            glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

            if (depthTest) glEnable(GL_DEPTH_TEST);
            else glDisable(GL_DEPTH_TEST);

            if (cullFace) glEnable(GL_CULL_FACE);
            else glDisable(GL_CULL_FACE);
        }
    }

    @Override
    public void dispose() {
        if (disposed) return;

        glDeleteFramebuffers(framebuffer);
        glDeleteRenderbuffers(renderbuffer);

        disposed = true;
    }
}
