package com.crystal.engine.scene;

public final class SceneMaterialSource {

    private final String name;
    private final String albedo;
    private final String normal;
    private final Float roughness;
    private final Float metallic;
    private final Float normalStrength;

    public SceneMaterialSource(String name, String albedo, String normal,
                             Float roughness, Float metallic, Float normalStrength) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Material name cannot be null or blank");

        this.name = name;
        this.albedo = albedo;
        this.normal = normal;
        this.roughness = roughness;
        this.metallic = metallic;
        this.normalStrength = normalStrength;
    }

    public String getName() {
        return name;
    }

    public String getAlbedo() {
        return albedo;
    }

    public String getNormal() {
        return normal;
    }

    public Float getRoughness() {
        return roughness;
    }

    public Float getMetallic() {
        return metallic;
    }

    public Float getNormalStrength() {
        return normalStrength;
    }
}
