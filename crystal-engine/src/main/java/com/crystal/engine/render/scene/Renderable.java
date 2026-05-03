package com.crystal.engine.render.scene;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;

public class Renderable {

    private final Mesh mesh;
    private final Material material;

    public Renderable(Mesh mesh, Material material) {
        this.mesh = mesh;
        this.material = material;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Material getMaterial() {
        return material;
    }
}
