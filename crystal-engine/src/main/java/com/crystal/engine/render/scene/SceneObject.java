package com.crystal.engine.render.scene;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;

public class SceneObject {

    private String name;
    private final Mesh mesh;
    private final Material material;
    private final Transform transform;

    public SceneObject(String name, Mesh mesh, Material material, Transform transform) {
        this.name = name;
        this.mesh = mesh;
        this.material = material;
        this.transform = transform;
    }

    public String getName() {
        return name;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Material getMaterial() {
        return material;
    }

    public Transform getTransform() {
        return transform;
    }

    public SceneObject setName(String name) {
        this.name = name;
        return this;
    }
}
