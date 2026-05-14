package com.crystal.engine.render.scene;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class SceneObject {

    private String name;
    private final Mesh mesh;
    private final Material material;
    private final Transform transform;

    private SceneObject parent;
    private final List<SceneObject> children = new ArrayList<>();

    private boolean active = true;
    private boolean visible = true;

    private float boundingRadius = 1.0f;

    public SceneObject(String name, Mesh mesh, Material material, Transform transform) {
        if ((mesh == null) != (material == null))
            throw new IllegalArgumentException("Mesh and material must either both be present or both be null");

        if (transform == null) throw new IllegalArgumentException("Transform cannot be null");

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

    public boolean isRenderable() {
        return mesh != null && material != null;
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

    public SceneObject removeChild(SceneObject child) {
        if (child == null)
            return this;

        if (children.remove(child)) {
            child.parent = null;
            child.getTransform().setParent(null);
        }

        return this;
    }

    public SceneObject addChild(SceneObject child) {
        if (child == null)
            throw new IllegalArgumentException("Child cannot be null");

        if (child == this)
            throw new IllegalArgumentException("SceneObject cannot be parented to itself");

        if (children.contains(child))
            return this;

        if (child.isAncestorOf(this))
            throw new IllegalArgumentException("Cannot create circular scene hierarchy");

        if (child.parent != null)
            child.parent.removeChild(child);

        child.parent = this;
        child.getTransform().setParent(this.transform);
        children.add(child);

        return this;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isVisible() {
        return visible;
    }

    public float getBoundingRadius() {
        return boundingRadius;
    }

    public Vector3f getWorldBoundsCenter() {
        if (mesh == null)
            return transform.getWorldPosition();

        return transform.getWorldMatrix()
                .transformPosition(mesh.getBounds().center(), new Vector3f());
    }

    public float getWorldBoundingRadius() {
        if (mesh == null)
            return boundingRadius;

        Vector3f scale = transform.getWorldScale();

        float maxScale = Math.max(
                Math.abs(scale.x),
                Math.max(Math.abs(scale.y), Math.abs(scale.z))
        );

        return mesh.getBounds().radius() * maxScale;
    }

    public SceneObject setActive(boolean active) {
        this.active = active;
        return this;
    }

    public SceneObject setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public SceneObject setBoundingRadius(float boundingRadius) {
        this.boundingRadius = boundingRadius;
        return this;
    }
}
