package com.crystal.engine.render.shader;

public final class ShaderUniforms {
    public static final String DEBUG_VIEW_MODE = "debugViewMode";
    public static final String DEBUG_COLOR = "debugColor";
    public static final String EXPOSURE = "exposure";
    public static final String HAS_IBL = "hasIBL";

    public static final String MODEL = "model";
    public static final String VIEW = "view";
    public static final String PROJECTION = "projection";
    public static final String SKYBOX = "skybox";

    public static final String ALBEDO_TEXTURE = "albedoTexture";
    public static final String NORMAL_MAP = "normalMap";
    public static final String METALLIC_ROUGHNESS_MAP = "metallicRoughnessMap";
    public static final String AMBIENT_OCCLUSION_MAP = "ambientOcclusionMap";
    public static final String EMISSIVE_MAP = "emissiveMap";

    public static final String IRRADIANCE_MAP = "irradianceMap";
    public static final String PREFILTER_MAP = "prefilterMap";
    public static final String BRDF_LUT = "brdfLut";

    public static final String MATERIAL_TINT = "materialTint";
    public static final String MATERIAL_ROUGHNESS = "materialRoughness";
    public static final String MATERIAL_METALLIC = "materialMetallic";
    public static final String MATERIAL_NORMAL_STRENGTH = "materialNormalStrength";
    public static final String MATERIAL_EMISSIVE = "materialEmissive";

    public static final String HAS_ALBEDO_TEXTURE = "hasAlbedoTexture";
    public static final String HAS_NORMAL_MAP = "hasNormalMap";
    public static final String HAS_METALLIC_ROUGHNESS_MAP = "hasMetallicRoughnessMap";
    public static final String HAS_AO_MAP = "hasAoMap";
    public static final String HAS_EMISSIVE_MAP = "hasEmissiveMap";

    public static final String IBL_DIFFUSE_INTENSITY = "iblDiffuseIntensity";
    public static final String IBL_SPECULAR_INTENSITY = "iblSpecularIntensity";

    public static final String SHADOW_MAP = "shadowMap";
    public static final String LIGHT_SPACE_MATRIX = "lightSpaceMatrix";
    public static final String SHADOW_STRENGTH = "shadowStrength";
    public static final String HAS_SHADOWS = "hasShadows";

    private ShaderUniforms() {}
}
