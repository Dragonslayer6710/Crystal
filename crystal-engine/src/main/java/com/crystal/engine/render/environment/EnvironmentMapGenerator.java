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
    private final Mesh cube;

    private final int framebuffer;
    private final int renderbuffer;

    private boolean disposed;

    public EnvironmentMapGenerator(Shader equirectangularToCubemapShader,
                                   Shader irradianceConvolutionShader, Mesh cube) {
        if (equirectangularToCubemapShader == null)
            throw new IllegalArgumentException("Equirectangular conversion shader cannot be null");

        if (irradianceConvolutionShader == null)
            throw new IllegalArgumentException("Irradiance Convolution shader cannot be null");

        if (cube == null)
            throw new IllegalArgumentException("Cube mesh cannot be null");

        this.equirectangularToCubemapShader = equirectangularToCubemapShader;
        this.irradianceConvolutionShader = irradianceConvolutionShader;
        this.cube = cube;

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
                equirectangularToCubemapShader,
                equirectangularTexture,
                "equirectangularMap"
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
                irradianceConvolutionShader,
                environmentCubemap,
                "environmentMap"
        );
    }

    private Texture renderToCubemap(String debugname, int size, Shader shader,
                                    Texture inputTexture, String inputSamplerName) {
        Texture output = TextureFactory.createCubemap(
                size,
                TextureSettings.defaultHDR(),
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
            drawCube();
        }

        glEnable(GL_CULL_FACE);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return output;
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

    private void drawCube() {
        cube.bind();

        if (cube.isIndexed()) {
            glDrawElements(
                    cube.getPrimTypeValue(),
                    cube.getIndexCount(),
                    GL_UNSIGNED_INT,
                    0
            );
        } else {
            glDrawArrays(
                    cube.getPrimTypeValue(),
                    0,
                    cube.getVertexCount()
            );
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
