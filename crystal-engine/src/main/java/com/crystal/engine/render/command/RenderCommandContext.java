package com.crystal.engine.render.command;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.material.RenderState;
import com.crystal.engine.render.mesh.Mesh;

public interface RenderCommandContext {

    void applyRenderState(RenderState state);

    void bindMaterial(Material material);

    void bindMesh(Mesh mesh);

    float getAspectRatio();
}
