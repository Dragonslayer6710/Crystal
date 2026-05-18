package com.crystal.engine.render.scene;

public abstract class SceneComponent {

    private SceneObject owner;
    private boolean enabled = true;

    final void attach(SceneObject owner) {
        if (owner == null) throw new IllegalArgumentException("Owner cannot be null");
        if (this.owner != null) throw new IllegalStateException("Component is already attached");

        this.owner = owner;
        onAttach(owner);
    }

    final void detach() {
        SceneObject previousOwner = owner;
        owner = null;
        onDetach(previousOwner);
    }

    public SceneObject getOwner() {
        return owner;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public SceneComponent setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    protected void onAttach(SceneObject owner) {}

    protected void onDetach(SceneObject owner) {}

    public void update(SceneUpdateContext context) {}
}
