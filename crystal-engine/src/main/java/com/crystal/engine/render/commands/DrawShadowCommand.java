package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderContext;
import com.crystal.engine.render.gl.MeshRenderer;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.shader.ShaderUniforms;
import org.joml.Matrix4f;

public final class DrawShadowCommand implements RenderCommand{

    private final SceneObject object;
    private final Shader shader;
    private final Matrix4f lightSpaceMatrix;

    public DrawShadowCommand(SceneObject object, Shader shader, Matrix4f lightSpaceMatrix) {
        if (object == null) throw new IllegalArgumentException("SceneObject cannot be null");
        if (!object.isRenderable()) throw new IllegalArgumentException("SceneObject must be renderable");
        if (shader == null) throw new IllegalArgumentException("Shader cannot be null");
        if (lightSpaceMatrix == null) throw new IllegalArgumentException("Light-space matrix cannot be null");

        this.object = object;
        this.shader = shader;
        this.lightSpaceMatrix = new Matrix4f(lightSpaceMatrix);
    }

    @Override
    public void execute(RenderContext context) {
        shader.bind();

        shader.setMat4(ShaderUniforms.LIGHT_SPACE_MATRIX, lightSpaceMatrix);
        shader.setMat4(ShaderUniforms.MODEL, object.getTransform().getWorldMatrix());

        MeshRenderer.draw(object.getMesh());
    }
}
