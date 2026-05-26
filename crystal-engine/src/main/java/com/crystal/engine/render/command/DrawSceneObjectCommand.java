package com.crystal.engine.render.command;

import com.crystal.engine.render.opengl.MeshRenderer;
import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.shader.ShaderUniforms;

public final class DrawSceneObjectCommand implements RenderCommand {

    private final SceneObject object;

    public DrawSceneObjectCommand(SceneObject object) {
        this.object = object;
    }

    @Override
    public void execute(RenderCommandContext context) {
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
