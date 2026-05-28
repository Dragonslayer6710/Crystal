package com.crystal.engine.render.command;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.opengl.MeshRenderer;
import com.crystal.engine.render.shader.Shader;
import com.crystal.engine.render.shader.ShaderUniforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class DrawDebugBoxCommand implements RenderCommand {

    private final Mesh mesh;
    private final Material material;
    private final Matrix4f modelMatrix;
    private final Vector3f color;

    public DrawDebugBoxCommand(Mesh mesh, Material material, Matrix4f modelMatrix, Vector3f color) {
        if (mesh == null) throw new IllegalArgumentException("Mesh cannot be null");
        if (material == null) throw new IllegalArgumentException("Material cannot be null");
        if (modelMatrix == null) throw new IllegalArgumentException("Model matrix cannot be null");
        if (color == null) throw new IllegalArgumentException("Color cannot be null");

        this.mesh = mesh;
        this.material = material;
        this.modelMatrix = new Matrix4f(modelMatrix);
        this.color = new Vector3f(color);
    }

    @Override
    public void execute(RenderCommandContext context) {
        Shader shader = material.getShader();

        context.applyRenderState(material.getRenderState());
        context.bindMaterial(material);

        shader.setMat4(ShaderUniforms.MODEL, modelMatrix);
        shader.setVec3(ShaderUniforms.DEBUG_COLOR, color.x, color.y, color.z);

        context.bindMesh(mesh);
        MeshRenderer.draw(mesh);
    }
}
