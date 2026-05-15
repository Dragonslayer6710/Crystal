package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderContext;
import com.crystal.engine.render.gl.MeshRenderer;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.shader.ShaderUniforms;

import static org.lwjgl.opengl.GL46.*;

public final class DrawSceneObjectCommand implements RenderCommand {

    private final SceneObject object;

    public DrawSceneObjectCommand(SceneObject object) {
        this.object = object;
    }

    @Override
    public void execute(RenderContext context) {
        var material = object.getMaterial();
        var mesh = object.getMesh();
        var transform = object.getTransform();
        Shader shader = material.getShader();

        context.applyRenderState(material.getRenderState());
        context.bindMaterial(material);

        shader.setMat4(ShaderUniforms.MODEL, transform.getWorldMatrix());

        context.bindMesh(mesh);
        MeshRenderer.draw(mesh);
    }
}
