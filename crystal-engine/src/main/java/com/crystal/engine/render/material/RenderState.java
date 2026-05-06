package com.crystal.engine.render.material;

public class RenderState {

    private boolean wireframe = false;
    private boolean cullFace = true;

    public boolean isWireframe() {
        return wireframe;
    }

    public RenderState setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
        return this;
    }

    public boolean isCullFace() {
        return cullFace;
    }

    public RenderState setCullFace(boolean cullFace) {
        this.cullFace = cullFace;
        return this;
    }
}
