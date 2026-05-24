#version 460 core

in vec3 v_WorldPosition;
in vec2 v_UV;
in vec3 v_Normal;
in vec3 v_Tangent;
in vec4 v_LightSpacePosition;

uniform int debugViewMode;
uniform float exposure;
uniform int hasIBL;

uniform float iblDiffuseIntensity;
uniform float iblSpecularIntensity;

uniform sampler2D albedoTexture;
uniform sampler2D normalMap;
uniform sampler2D metallicRoughnessMap;
uniform sampler2D ambientOcclusionMap;
uniform sampler2D emissiveMap;

uniform samplerCube irradianceMap;
uniform samplerCube prefilterMap;
uniform sampler2D brdfLut;

uniform sampler2D shadowMap;

layout (std140, binding = 0) uniform SceneData {
    mat4 view;
    mat4 projection;

    vec4 ambient;        // rgb = color, a = intensity
    vec4 cameraPosition; // xyz = world position

    vec4 sunDirection;   // xyz = direction
    vec4 sunColor;       // rgb = color, a = intensity

    mat4 lightSpaceMatrix;
};

uniform vec3 materialTint;
uniform float materialRoughness;
uniform float materialMetallic;
uniform float materialNormalStrength;
uniform vec3 materialEmissive;

uniform int hasAlbedoTexture;
uniform int hasNormalMap;
uniform int hasMetallicRoughnessMap;
uniform int hasAoMap;
uniform int hasEmissiveMap;

uniform float shadowStrength;

out vec4 f_Color;

const float PI = 3.14159265358979;
const float MAX_REFLECTION_LOD = 4.0;

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness) {
    return F0 + (max(vec3(1.0 - roughness), F0) - F0)
        * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

float distributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;

    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH * NdotH;

    float denominator = NdotH2 * (a2 - 1.0) + 1.0;
    denominator = PI * denominator * denominator;

    return a2 / max(denominator, 0.0001);
}

float geometrySchlickGGX(float NdotV, float roughness) {
    float r = roughness + 1.0;
    float k = (r * r) / 8.0;

    return NdotV / (NdotV * (1.0 - k) + k);
}

float geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);

    float ggxV = geometrySchlickGGX(NdotV, roughness);
    float ggxL = geometrySchlickGGX(NdotL, roughness);

    return ggxV * ggxL;
}

vec3 toneMapReinhard(vec3 value) {
    return value / (value + vec3(1.0));
}

vec3 gammaCorrect(vec3 value) {
    return pow(value, vec3(1.0 / 2.2));
}

vec3 getNormal() {
    vec3 N = normalize(v_Normal);
    vec3 T = normalize(v_Tangent);
    T = normalize(T - dot(T, N) * N);
    vec3 B = normalize(cross(N, T));

    mat3 TBN = mat3(T, B, N);

    vec3 sampledNormal = texture(normalMap, v_UV).xyz * 2.0 - 1.0;
    sampledNormal.xy *= materialNormalStrength;
    sampledNormal = normalize(sampledNormal);

    return normalize(TBN * sampledNormal);
}

vec2 getMetallicRoughness() {
    vec4 mrSample = texture(metallicRoughnessMap, v_UV);

    float roughness = clamp(materialRoughness * mrSample.g, 0.0, 1.0);
    float metallic = clamp(materialMetallic * mrSample.b, 0.0, 1.0);

    return vec2(metallic, roughness);
}

vec3 calculateIBLSpecular(vec3 albedo, vec3 normal, vec3 viewDirection, float metallic, float roughness) {
    if (hasIBL != 1)
        return vec3(0.0);

    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallic);

    float NdotV = max(dot(normal, viewDirection), 0.0);
    vec3 F = fresnelSchlickRoughness(NdotV, F0, roughness);

    vec3 R = reflect(-viewDirection, normal);
    vec3 prefilteredColor = textureLod(prefilterMap, R, roughness * MAX_REFLECTION_LOD).rgb;
    vec2 brdf = texture(brdfLut, vec2(NdotV, roughness)).rg;

    return prefilteredColor * (F * brdf.x + brdf.y) * iblSpecularIntensity;
}

float calculateShadow(vec4 lightSpacePosition, vec3 normal, vec3 lightDirection) {
    vec3 projected = lightSpacePosition.xyz / lightSpacePosition.w;
    projected = projected * 0.5 + 0.5;

    if (projected.z > 1.0)
    return 0.0;

    float closestDepth = texture(shadowMap, projected.xy).r;
    float currentDepth = projected.z;

    float bias = max(0.005 * (1.0 - dot(normal, lightDirection)), 0.0005);

    return currentDepth - bias > closestDepth ? 1.0 : 0.0;
}

vec3 calculateLighting(vec3 albedo, vec3 normal, float metallic, float roughness, float ao) {
    vec3 L = normalize(-sunDirection.xyz);
    vec3 V = normalize(cameraPosition.xyz - v_WorldPosition);
    vec3 H = normalize(L + V);

    float NdotL = max(dot(normal, L), 0.0);
    float NdotV = max(dot(normal, V), 0.0);

    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallic);

    vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);
    float D = distributionGGX(normal, H, roughness);
    float G = geometrySmith(normal, V, L, roughness);

    vec3 numerator = D * G * F;
    float denominator = 4.0 * NdotV * NdotL + 0.0001;
    vec3 specular = numerator / denominator;

    vec3 kS = F;
    vec3 kD = vec3(1.0) - kS;
    kD *= 1.0 - metallic;

    vec3 radiance = sunColor.rgb * sunColor.a;

    vec3 diffuseBRDF = kD * albedo / PI;

    vec3 directLighting = (diffuseBRDF + specular) * radiance * NdotL;
    float shadow = calculateShadow(v_LightSpacePosition, normal, L);
    directLighting *= (1.0 - shadow * shadowStrength);

    vec3 ambientLighting;

    if (hasIBL == 1) {
        vec3 irradiance = texture(irradianceMap, normal).rgb;
        vec3 diffuseIBL = irradiance * albedo;

        vec3 R = reflect(-V, normal);

        vec3 prefilteredColor = textureLod(prefilterMap, R, roughness * MAX_REFLECTION_LOD).rgb;

        vec2 brdf = texture(brdfLut, vec2(max(dot(normal, V), 0.0), roughness)).rg;

        vec3 roughnessFresnel = fresnelSchlickRoughness(max(dot(normal, V), 0.0), F0, roughness);
        vec3 specularIBL = prefilteredColor * (roughnessFresnel * brdf.x + brdf.y);

        ambientLighting = (diffuseIBL * kD  * iblDiffuseIntensity + specularIBL * iblSpecularIntensity) * ao;
    } else {
        ambientLighting = ambient.rgb * ambient.a * albedo * ao;
    }

    return ambientLighting + directLighting;
}

void main() {
    vec3 N = hasNormalMap == 1 ? getNormal() : normalize(v_Normal);

    vec3 albedo = hasAlbedoTexture == 1
    ? texture(albedoTexture, v_UV).rgb * materialTint
    : materialTint;

    vec2 mr = hasMetallicRoughnessMap == 1
    ? getMetallicRoughness()
    : vec2(materialMetallic, materialRoughness);
    float metallic = mr.x;
    float roughness = mr.y;

    float ao = hasAoMap == 1
    ? texture(ambientOcclusionMap, v_UV).r
    : 1.0;

    vec3 emissive = hasEmissiveMap == 1
    ? texture(emissiveMap, v_UV).rgb * materialEmissive
    : materialEmissive;

    switch (debugViewMode) {
        case 0:
            vec3 finalColor = calculateLighting(albedo, N, metallic, roughness, ao);
            finalColor += emissive;

            vec3 mapped = toneMapReinhard(finalColor * exposure);

            f_Color = vec4(mapped, 1.0);
            break;

        case 1:
            f_Color = vec4(albedo, 1.0);
            break;

        case 2:
            f_Color = vec4(N * 0.5 + 0.5, 1.0);
            break;

        case 3:
            f_Color = vec4(vec3(metallic), 1.0);
            break;

        case 4:
            f_Color = vec4(vec3(roughness), 1.0);
            break;

        case 5:
            f_Color = vec4(vec3(ao), 1.0);
            break;

        case 6:
            f_Color = vec4(emissive, 1.0);
            break;

        case 7:
            if (hasIBL == 1) {
                f_Color = vec4(texture(irradianceMap, N).rgb, 1.0);
            } else {
                f_Color = vec4(1.0, 0.0, 1.0, 1.0);
            }
            break;

        case 8:
            if (hasIBL == 1) {
                vec3 R = reflect(-normalize(cameraPosition.xyz - v_WorldPosition), N);
                f_Color = vec4(texture(prefilterMap, R).rgb, 1.0);
            } else {
                f_Color = vec4(1.0, 0.0, 1.0, 1.0);
            }
            break;

        case 9:
            if (hasIBL == 1){
                vec3 V = normalize(cameraPosition.xyz - v_WorldPosition);
                float NdotV = max(dot(N, V), 0.0);

                vec2 brdf = texture(
                brdfLut,
                vec2(NdotV, roughness)
                ).rg;

                f_Color = vec4(brdf, 0.0, 1.0);
            } else {
                f_Color = vec4(1.0, 0.0, 1.0, 1.0);
            }
            break;

        case 10:
            float shadow = calculateShadow(
            v_LightSpacePosition,
            N,
            normalize(-sunDirection.xyz)
            );

            f_Color = vec4(vec3(shadow), 1.0);
            break;

        case 11:
            vec3 V = normalize(cameraPosition.xyz - v_WorldPosition);
            vec3 iblSpecular = calculateIBLSpecular(albedo, N, V, metallic, roughness);
            f_Color = vec4(toneMapReinhard(iblSpecular * exposure), 1.0);
            break;

        default:
            f_Color = vec4(1.0, 0.0, 1.0, 1.0);
            break;
    }
}
