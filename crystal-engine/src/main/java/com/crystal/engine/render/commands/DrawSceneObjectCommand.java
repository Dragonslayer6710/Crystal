package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderContext;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.shader.ShaderUniforms;

import static org.lwjgl.opengl.GL46.*;

public class DrawSceneObjectCommand implements RenderCommand {

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
        context.bindMesh(mesh);

        shader.setMat4(ShaderUniforms.MODEL, transform.getWorldMatrix());

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
}
