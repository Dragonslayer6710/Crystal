package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderContext;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Scene;

import static org.lwjgl.opengl.GL46.*;

public class DrawSceneObjectCommand implements RenderCommand {

    private final SceneObject object;
    private final Scene scene;
    private final float aspectRatio;

    public DrawSceneObjectCommand(SceneObject object, Scene scene, float aspectRatio) {
        this.object = object;
        this.scene = scene;
        this.aspectRatio = aspectRatio;
    }

    @Override
    public void execute(RenderContext context) {
        var material = object.getMaterial();
        var mesh = object.getMesh();
        var transform = object.getTransform();
        var shader = material.getShaderProgram();

        context.applyRenderState(material.getRenderState());
        context.bindMaterial(material);
        context.bindScene(shader, scene, aspectRatio);
        context.bindMesh(mesh);

        shader.setMat4("model", transform.getWorldMatrix());

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
