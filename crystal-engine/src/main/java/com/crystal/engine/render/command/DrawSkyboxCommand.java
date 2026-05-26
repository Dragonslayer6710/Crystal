package com.crystal.engine.render.command;

import com.crystal.engine.render.opengl.GLStateSnapshot;
import com.crystal.engine.render.opengl.MeshRenderer;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.scene.Scene;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.shader.ShaderUniforms;
import com.crystal.engine.render.texture.Texture;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL46.*;

public final class DrawSkyboxCommand implements RenderCommand {

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
    public void execute(RenderCommandContext context) {
        Texture skybox = scene.getEnvironment().getSkybox();

        if (skybox == null)
            return;

        GLStateSnapshot snapshot = new GLStateSnapshot();

        try {
            glDepthFunc(GL_LEQUAL);
            glDepthMask(false);

            shader.bind();

            shader.setInt(ShaderUniforms.SKYBOX, 0);

            Matrix4f view = new Matrix4f(
                    scene.getCamera().getViewMatrix()
            );

            view.m30(0.0f);
            view.m31(0.0f);
            view.m32(0.0f);

            shader.setMat4(ShaderUniforms.VIEW, view);
            shader.setMat4(
                    ShaderUniforms.PROJECTION,
                    scene.getCamera().getProjectionMatrix(
                            context.getAspectRatio()
                    )
            );

            skybox.bind(0);

            context.bindMesh(cubeMesh);
            MeshRenderer.draw(cubeMesh);
        } finally {
            snapshot.restore();
        }
    }
}
