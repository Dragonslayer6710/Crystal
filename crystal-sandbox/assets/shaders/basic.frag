#version 460 core

in vec3 v_WorldPosition;
in vec3 v_Color;
in vec2 v_UV;
in vec3 v_Normal;
in vec3 v_Tangent;

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
uniform float materialEmissive;

out vec4 color;

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

vec3 calculateLighting(vec3 albedo, vec3 normal, float metallic, float roughness) {
    vec3 L = normalize(-sunDirection.xyz);
    vec3 V = normalize(cameraPosition.xyz - v_WorldPosition);
    vec3 H = normalize(L + V);

    float diffuse = max(dot(normal, L), 0.0);

    float shininess = mix(128.0, 8.0, roughness);
    float specular = pow(max(dot(normal, H), 0.0), shininess) * (1.0 - roughness);

    float ao = texture(ambientOcclusionMap, v_UV).r;

    vec3 ambientLight = ambient.rgb * ambient.a * ao;
    vec3 diffuseLight = sunColor.rgb * sunColor.a * diffuse;
    vec3 specularLight = sunColor.rgb * sunColor.a * specular;

    return albedo * (ambientLight + diffuseLight) * mix(1.0, 0.35, metallic) + specularLight;
}

void main() {
    vec3 N = getNormal();

    vec3 albedo = texture(albedoTexture, v_UV).rgb * v_Color * materialTint;

    vec2 mr = getMetallicRoughness();

    vec3 finalColor = calculateLighting(albedo, N, mr.x, mr.y);

    vec3 emissive = texture(emissiveMap, v_UV).rgb * materialEmissive;
    finalColor += emissive;

    // Simple Reinhard tone mapping
    vec3 mapped = finalColor / (finalColor + vec3(1.0));

    // Gamma correction
    mapped = pow(mapped, vec3(1.0 / 2.2));

    color = vec4(mapped, 1.0);
}