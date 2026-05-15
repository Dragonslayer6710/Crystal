package com.crystal.engine.render;

public class RendererConfig {

    private boolean depthTest = true;
    private boolean faceCulling = true;
    private boolean frustumCulling = true;
    private boolean shadowsEnabled = true;
    private int shadowMapSize = 2048;

    public boolean isDepthTest() { return depthTest; }
    public boolean isFaceCulling() { return faceCulling; }
    public boolean isFrustumCulling() { return frustumCulling; }
    public boolean isShadowsEnabled() { return shadowsEnabled; }
    public int getShadowMapSize() { return shadowMapSize; }

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

    public RendererConfig setShadowsEnabled(boolean shadowsEnabled) {
        this.shadowsEnabled = shadowsEnabled;
        return this;
    }

    public RendererConfig setShadowMapSize(int shadowMapSize) {
        if (shadowMapSize <= 0)
            throw new IllegalArgumentException("Shadow map size must be greater than 0");

        this.shadowMapSize = shadowMapSize;
        return this;
    }
}
