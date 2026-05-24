package com.crystal.engine.render.environment;

import com.crystal.engine.render.texture.Texture;
import org.joml.Vector3f;

public class Environment {

    private final Vector3f ambientColor = new Vector3f(0.03f);
    private float ambientIntensity = 1.0f;

    private Texture skybox;
    private Texture irradianceMap;
    private Texture prefilterMap;
    private Texture brdfLut;

    private float iblDiffuseIntensity = 1.0f;
    private float iblSpecularIntensity = 1.0f;

    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    public float getAmbientIntensity() {
        return ambientIntensity;
    }

    public Texture getSkybox() {
        return skybox;
    }

    public Texture getIrradianceMap() {
        return irradianceMap;
    }

    public Texture getPrefilterMap() {
        return prefilterMap;
    }

    public Texture getBrdfLut() {
        return brdfLut;
    }

    public float getIblDiffuseIntensity() {
        return iblDiffuseIntensity;
    }

    public float getIblSpecularIntensity() {
        return iblSpecularIntensity;
    }

    public Environment setAmbientColor(float r, float g, float b) {
        ambientColor.set(r, g, b);
        return this;
    }

    public Environment setAmbientIntensity(float intensity) {
        this.ambientIntensity = intensity;
        return this;
    }

    public Environment setSkybox(Texture skybox) {
        this.skybox = skybox;
        return this;
    }

    public Environment setIrradianceMap(Texture irradianceMap) {
        this.irradianceMap = irradianceMap;
        return this;
    }

    public Environment setPrefilterMap(Texture prefilterMap) {
        this.prefilterMap = prefilterMap;
        return this;
    }

    public Environment setBrdfLut(Texture brdfLut) {
        this.brdfLut = brdfLut;
        return this;
    }

    public Environment setIblDiffuseIntensity(float iblDiffuseIntensity) {
        validateIntensity(iblDiffuseIntensity, "IBL diffuse intensity");
        this.iblDiffuseIntensity = iblDiffuseIntensity;
        return this;
    }

    public Environment setIblSpecularIntensity(float iblSpecularIntensity) {
        validateIntensity(iblSpecularIntensity, "IBL specular intensity");
        this.iblSpecularIntensity = iblSpecularIntensity;
        return this;
    }

    public Environment copyLightingFrom(Environment source) {
        if (source == null) throw new IllegalArgumentException("Source environment cannot be null");

        skybox = source.skybox;
        irradianceMap = source.irradianceMap;
        prefilterMap = source.prefilterMap;
        brdfLut = source.brdfLut;
        iblDiffuseIntensity = source.iblDiffuseIntensity;
        iblSpecularIntensity = source.iblSpecularIntensity;

        return this;
    }

    public boolean hasSkybox() {
        return skybox != null;
    }

    public boolean hasDiffuseIBL() {
        return irradianceMap != null;
    }

    public boolean hasSpecularIBL() {
        return prefilterMap != null && brdfLut != null;
    }

    public boolean hasIBL() {
        return hasDiffuseIBL() && hasSpecularIBL();
    }

    private static void validateIntensity(float value, String name) {
        if (!Float.isFinite(value) || value < 0.0f)
            throw new IllegalArgumentException(name + " must be finite and non-negative");
    }
}
