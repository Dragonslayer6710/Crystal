package com.crystal.engine.render.material;

public class RenderState {

    private boolean wireframe = false;
    private boolean cullFace = true;
    private boolean depthTest = true;

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

    public boolean isDepthTest() {
        return depthTest;
    }

    public RenderState setDepthTest(boolean depthTest) {
        this.depthTest = depthTest;
        return this;
    }

    public int getSortKey() {
        int key = 0;

        if (depthTest) key |= 1;
        if (cullFace) key |= 1 << 1;
        if (wireframe) key |= 1 << 2;

        return key;
    }
}
