#version 460 core

in vec3 v_WorldPosition;
in vec3 v_Color;
in vec2 v_UV;
in vec3 v_Normal;
in vec3 v_Tangent;

uniform sampler2D albedoTexture;
uniform sampler2D normalMap;

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

out vec4 color;

void main() {
    vec3 N = normalize(v_Normal);
    vec3 T = normalize(v_Tangent);
    T = normalize(T - dot(T, N) * N);
    vec3 B = normalize(cross(N, T));

    mat3 TBN = mat3(T, B, N);

    vec3 sampledNormal = texture(normalMap, v_UV).xyz * 2.0 - 1.0;
    N = normalize(TBN * sampledNormal);

    vec3 L = normalize(-sunDirection.xyz);
    vec3 V = normalize(cameraPosition.xyz - v_WorldPosition);
    vec3 H = normalize(L + V);

    vec3 albedo = texture(albedoTexture, v_UV).rgb * materialTint;

    float diffuse = max(dot(N, L), 0.0);

    float shininess = mix(128.0, 8.0, materialRoughness);
    float specular = pow(max(dot(N, H), 0.0), shininess) * (1.0 - materialRoughness);

    vec3 ambientLight = ambient.rgb * ambient.a;
    vec3 diffuseLight = sunColor.rgb * sunColor.a * diffuse;
    vec3 specularLight = sunColor.rgb * sunColor.a * specular;

    vec3 finalColor = albedo * (ambientLight + diffuseLight) + specularLight;

    color = vec4(finalColor, 1.0);
}