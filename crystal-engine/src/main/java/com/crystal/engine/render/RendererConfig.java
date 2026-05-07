package com.crystal.engine.render;

public class RendererConfig {

    private boolean depthTest = true;
    private boolean faceCulling = true;
    private boolean frustumCulling = true;

    public boolean isDepthTest() { return depthTest; }
    public boolean isFaceCulling() { return faceCulling; }
    public boolean isFrustumCulling() { return frustumCulling; }

    public RendererConfig setDepthTest(boolean depthTest) {
        this.depthTest = depthTest;
        return this;
    }

    public RendererConfig setFaceCulling(boolean faceCulling) {
        this.faceCulling = faceCulling;
        return this;
    }

    public RendererConfig setFrustumCulling(boolean frustumCulling) {
        this.frustumCulling = frustumCulling;
        return this;
    }

}
