package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderContext;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.scene.Scene;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.texture.Texture;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL46.*;

public class DrawSkyboxCommand implements RenderCommand {

    private final Scene scene;
    private final Shader shader;
    private final Mesh cubeMesh;

    public DrawSkyboxCommand(Scene scene, Shader shader, Mesh cubeMesh) {
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");
        if (shader == null) throw new IllegalArgumentException("Shader cannot be null");
        if (cubeMesh == null) throw new IllegalArgumentException("Cube mesh cannot be null");

        this.scene = scene;
        this.shader = shader;
        this.cubeMesh = cubeMesh;
    }

    @Override
    public void execute(RenderContext context) {
        Texture skybox = scene.getEnvironment().getSkybox();

        if (skybox == null)
            return;

        glDepthFunc(GL_LEQUAL);
        glDepthMask(false);

        shader.bind();

        shader.setInt("skybox", 0);

        Matrix4f view = new Matrix4f(
                scene.getCamera().getViewMatrix()
        );

        view.m30(0.0f);
        view.m31(0.0f);
        view.m32(0.0f);

        shader.setMat4("view", view);
        shader.setMat4(
                "projection",
                scene.getCamera().getProjectionMatrix(
                        context.getAspectRatio()
                )
        );

        skybox.bind(0);

        cubeMesh.bind();

        if (cubeMesh.isIndexed()) {
            glDrawElements(
                    cubeMesh.getPrimTypeValue(),
                    cubeMesh.getIndexCount(),
                    GL_UNSIGNED_INT,
                    0
            );
        } else {
            glDrawArrays(
                    cubeMesh.getPrimTypeValue(),
                    0,
                    cubeMesh.getVertexCount()
            );
        }

        glDepthMask(true);
        glDepthFunc(GL_LESS);
    }
}
