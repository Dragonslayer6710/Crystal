package com.crystal.engine.render.scene;

import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import org.joml.Vector3f;

import java.util.*;

public class SceneObject {

    private String name;
    private final Mesh mesh;
    private final Material material;
    private final Transform transform;

    private SceneObject parent;
    private final List<SceneObject> children = new ArrayList<>();

    private final List<SceneComponent> components = new ArrayList<>();

    private final Set<String> tags = new HashSet<>();

    private boolean active = true;
    private boolean visible = true;
    private boolean castsShadow = true;

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
        return Collections.unmodifiableList(children);
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

    public SceneObject addComponent(SceneComponent component) {
        if (component == null) throw new IllegalArgumentException("Component cannot be null");
        if (component.getOwner() != null) throw new IllegalStateException("Component is already attached");

        components.add(component);
        component.attach(this);

        return this;
    }

    public boolean removeComponent(SceneComponent component) {
        if (component == null)
            return false;

        boolean removed = components.remove(component);

        if (removed)
            component.detach();

        return removed;
    }

    public List<SceneComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public <T extends SceneComponent> T getComponent(Class<T> type) {
        if (type == null) throw new IllegalArgumentException("Component type cannot be null");

        for (SceneComponent component : components) {
            if (type.isInstance(component))
                return type.cast(component);
        }

        return null;
    }

    public void update(SceneUpdateContext context) {
        if (!active)
            return;

        for (SceneComponent component : components) {
            component.updateComponent(context);
        }

        for (SceneObject child : children)
            child.update(context);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean castsShadow() {
        return castsShadow;
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

    public SceneObject setCastsShadow(boolean castsShadow) {
        this.castsShadow = castsShadow;
        return this;
    }

    public SceneObject setCastsShadowRecursive(boolean castsShadow) {
        setCastsShadow(castsShadow);

        for (SceneObject child : children)
            child.setCastsShadowRecursive(castsShadow);

        return this;
    }

    public SceneObject setBoundingRadius(float boundingRadius) {
        this.boundingRadius = boundingRadius;
        return this;
    }

    public SceneObject addTag(String tag) {
        tags.add(validateTag(tag));
        return this;
    }

    public SceneObject removeTag(String tag) {
        tags.remove(validateTag(tag));
        return this;
    }

    public boolean hasTag(String tag) {
        return tags.contains(validateTag(tag));
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    private static String validateTag(String tag) {
        if (tag == null || tag.isBlank()) throw new IllegalArgumentException("Tag cannot be null or blank");

        return tag;
    }

    public SceneObject copyHierarchy() {
        SceneObject copy = new SceneObject(name, mesh, material, transform.copy())
            .setActive(active)
            .setVisible(visible)
            .setCastsShadow(castsShadow)
            .setBoundingRadius(boundingRadius);

        for (String tag : tags)
            copy.addTag(tag);

        for (SceneObject child : children)
            copy.addChild(child.copyHierarchy());

        return copy;
    }
}
