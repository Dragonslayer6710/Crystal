package com.crystal.engine.render.scene;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;

import java.util.ArrayList;
import java.util.List;

public class SceneObject {

    private String name;
    private final Mesh mesh;
    private final Material material;
    private final Transform transform;

    private SceneObject parent;
    private final List<SceneObject> children = new ArrayList<>();

    public SceneObject(String name, Mesh mesh, Material material, Transform transform) {
        this.name = name;
        this.mesh = mesh;
        this.material = material;
        this.transform = transform;
    }

    public String getName() {
        return name;
    }

    public SceneObject setName(String name) {
        this.name = name;
        return this;
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

    public SceneObject getParent() {
        return parent;
    }

    public List<SceneObject> getChildren() {
        return children;
    }

    private boolean isAncestorOf(SceneObject object) {
        SceneObject current = object.parent;

        while (current != null) {
            if (current == this)
                return true;

            current = current.parent;
        }

        return false;
    }

    public SceneObject addChild(SceneObject child) {
        if (child == null)
            throw new IllegalArgumentException("Child cannot be null");

        if (child == this)
            throw new IllegalArgumentException("SceneObject cannot be parented to itself");

        if (child.isAncestorOf(this))
            throw new IllegalArgumentException("Cannot create circular scene hierarchy");

        if (child.parent != null)
            child.parent.children.remove(child);

        child.parent = this;
        child.getTransform().setParent(this.transform);
        children.add(child);

        return this;
    }
}
