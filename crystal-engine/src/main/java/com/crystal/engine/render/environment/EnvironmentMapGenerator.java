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
    private final Mesh cube;

    private final int framebuffer;
    private final int renderbuffer;

    private boolean disposed;

    public EnvironmentMapGenerator(Shader equirectangularToCubemapShader, Mesh cube) {
        if (equirectangularToCubemapShader == null)
            throw new IllegalArgumentException("Equirectangular conversion shader cannot be null");

        if (cube == null)
            throw new IllegalArgumentException("Cube mesh cannot be null");

        this.equirectangularToCubemapShader = equirectangularToCubemapShader;
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

        if (size <= 0)
            throw new IllegalArgumentException("Cubemap size must be greater than 0");

        Texture cubemap = TextureFactory.createCubemap(
                size,
                TextureSettings.defaultHDR(),
                "<generated:environment-cubemap>"
        );

        glNamedRenderbufferStorage(
                renderbuffer,
                GL_DEPTH_COMPONENT24,
                size,
                size
        );

        Matrix4f projection = new Matrix4f()
                .perspective((float) Math.toRadians(90.0f), 1.0f, 0.1f, 10.0f);

        Matrix4f[] views = {
                new Matrix4f().lookAt(0, 0, 0,  1,  0,  0,  0, -1,  0),
                new Matrix4f().lookAt(0, 0, 0, -1,  0,  0,  0, -1,  0),
                new Matrix4f().lookAt(0, 0, 0,  0,  1,  0,  0,  0,  1),
                new Matrix4f().lookAt(0, 0, 0,  0, -1,  0,  0,  0,  0, -1),
                new Matrix4f().lookAt(0, 0, 0,  0,  0,  1,  0, -1,  0),
                new Matrix4f().lookAt(0, 0, 0,  0,  0, -1,  0, -1,  0)
        };

        glViewport(0, 0, size, size);
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

        equirectangularToCubemapShader.bind();
        equirectangularToCubemapShader.setInt("equirectangularMap", 0);
        equirectangularToCubemapShader.setMat4("equirectangularMap", projection);

        equirectangularTexture.bind(0);

        glDisable(GL_CULL_FACE);;

        for (int face = 0; face < 6; face++) {
            equirectangularToCubemapShader.setMat4("view", views[face]);

            glNamedFramebufferTextureLayer(
                    framebuffer,
                    GL_COLOR_ATTACHMENT0,
                    cubemap.getId(),
                    0,
                    face
            );

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

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

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return cubemap;
    }

    public Texture generateCubemap(Texture equirectangularTexture) {
        return generateCubemap(equirectangularTexture, DEFAULT_CUBEMAP_SIZE);
    }

    @Override
    public void dispose() {
        if (disposed) return;

        glDeleteFramebuffers(framebuffer);
        glDeleteRenderbuffers(renderbuffer);

        disposed = true;
    }
}
