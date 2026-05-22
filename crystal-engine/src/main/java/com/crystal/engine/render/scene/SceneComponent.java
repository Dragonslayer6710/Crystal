package com.crystal.engine.render.scene;

public abstract class SceneComponent {

    private SceneObject owner;
    private boolean enabled = true;
    private boolean started;

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

    final void updateComponent(SceneUpdateContext context) {
        if (context == null) throw new IllegalArgumentException("SceneUpdateContext cannot be null");
        if (!enabled)
            return;

        if (!started) {
            started = true;
            onStart(context);
        }

        update(context);
    }

    public SceneObject getOwner() {
        return owner;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public SceneComponent setEnabled(boolean enabled) {
        if (this.enabled == enabled)
            return this;

        this.enabled = enabled;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }

        return this;
    }

    protected void onAttach(SceneObject owner) {}

    protected void onDetach(SceneObject owner) {}

    protected void onStart(SceneUpdateContext context) {}

    protected void onEnable() {}

    protected void onDisable() {}

    public void update(SceneUpdateContext context) {}
}
