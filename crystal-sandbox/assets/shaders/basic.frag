#version 460 core

in vec3 v_WorldPosition;
in vec3 v_Color;
in vec2 v_UV;
in vec3 v_Normal;
in vec3 v_Tangent;


uniform int debugViewMode;

uniform sampler2D albedoTexture;
uniform sampler2D normalMap;
uniform sampler2D metallicRoughnessMap;
uniform sampler2D ambientOcclusionMap;
uniform sampler2D emissiveMap;

layout (std140, binding = 0) uniform SceneData {
    mat4 view;
    mat4 projection;

    vec4 ambient;        // rgb = color, a = intensity
    vec4 cameraPosition; // xyz = world position

    vec4 sunDirection;   // xyz = direction
    vec4 sunColor;       // rgb = color, a = intensity
};

uniform vec3 materialTint;
uniform float materialRoughness;
uniform float materialMetallic;
uniform vec3 materialEmissive;

out vec4 color;

const float PI = 3.14159265358979;

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

float distributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;

    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH * NdotH;

    float denominator = NdotH * (a2 - 1.0) + 1.0;
    denominator = PI * denominator * denominator;

    return a2 / max(denominator, 0.0001);
}

float geometrySchlickGGX(float NdotV, float roughness) {
    float r = roughness + 1.0;
    float k = (r * r) / 8.0;

    return NdotV / (NdotV * (1.0 - k) + k);
}

vec3 getNormal() {
    vec3 N = normalize(v_Normal);
    vec3 T = normalize(v_Tangent);
    T = normalize(T - dot(T, N) * N);
    vec3 B = normalize(cross(N, T));

    mat3 TBN = mat3(T, B, N);

    vec3 sampledNormal = texture(normalMap, v_UV).xyz * 2.0 - 1.0;
    return normalize(TBN * sampledNormal);
}

vec2 getMetallicRoughness() {
    vec4 mrSample = texture(metallicRoughnessMap, v_UV);

    float roughness = clamp(materialRoughness * mrSample.g, 0.0, 1.0);
    float metallic = clamp(materialMetallic * mrSample.b, 0.0, 1.0);

    return vec2(metallic, roughness);
}

vec3 calculateLighting(vec3 albedo, vec3 normal, float metallic, float roughness, float ao) {
    // Ambient
    vec3 ambientLight = ambient.rgb * ambient.a * ao;

    // Diffuse
    vec3 L = normalize(-sunDirection.xyz);

    float diffuse = max(dot(normal, L), 0.0);
    vec3 diffuseLight = sunColor.rgb * sunColor.a * diffuse;

    // Specular
    vec3 F0 = vec3(0.04f);
    F0 = mix(F0, albedo, metallic);

    vec3 V = normalize(cameraPosition.xyz - v_WorldPosition);
    vec3 H = normalize(L + V);

    vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);
    float D = distributionGGX(normal, H, roughness);
    float G = geometrySchlickGGX(normal, V, L, roughness);

    float NdotL = max(dot(normal, L), 0.0);
    float NdotV = max(dot(normal, V), 0.0);

    vec3 numerator = D * G * F;
    float denominator = 4.0 * NdotV * NdotL + 0.0001;
    vec3 specular = numerator / denominator;

    vec3 specularLight = sunColor.rgb * sunColor.a * specular * NdotL;

    return albedo * (ambientLight + diffuseLight) * mix(1.0, 0.35, metallic) + specularLight;
}

void main() {
    vec3 N = getNormal();

    vec3 albedo = texture(albedoTexture, v_UV).rgb * v_Color * materialTint;

    vec2 mr = getMetallicRoughness();
    float metallic = mr.x;
    float roughness = mr.y;

    float ao = texture(ambientOcclusionMap, v_UV).r;
    vec3 emissive = texture(emissiveMap, v_UV).rgb * materialEmissive;

    switch (debugViewMode) {
        case 0:
            vec3 finalColor = calculateLighting(albedo, N, metallic, roughness, ao);
            finalColor += emissive;

            vec3 mapped = finalColor / (finalColor + vec3(1.0));
            mapped = pow(mapped, vec3(1.0 / 2.2));

            color = vec4(mapped, 1.0);
            break;
        case 1:
            color = vec4(albedo, 1.0);
            break;
        case 2:
            color = vec4(N * 0.5 + 0.5, 1.0);
            break;
        case 3:
            color = vec4(vec3(metallic), 1.0);
            break;
        case 4:
            color = vec4(vec3(roughness), 1.0);
            break;
        case 5:
            color = vec4(vec3(ao), 1.0);
            break;
        case 6:
            color = vec4(emissive, 1.0);
            break;
        default:
            color = vec4(1.0, 0.0, 1.0, 1.0);
            break;
    }
}