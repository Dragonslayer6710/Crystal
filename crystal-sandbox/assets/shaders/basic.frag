#version 460 core

in vec3 v_Color;
in vec2 v_UV;
in vec3 v_Normal;

uniform sampler2D albedoTexture;

layout (std140, binding = 0) uniform SceneData {
    mat4 view;
    mat4 projection;
    vec4 ambient;
    vec4 sunDirection;
    vec4 sunColor;
};

uniform vec3 materialTint;

out vec4 color;

void main() {
    vec3 normal = normalize(v_Normal);

    vec3 lightDir = normalize(-sunDirection.xyz);
    float diffuseFactor = max(dot(normal, lightDir), 0.0);

    vec3 albedo = texture(albedoTexture, v_UV).rgb * v_Color * materialTint;

    vec3 ambient = ambient.rgb * ambient.a;
    vec3 diffuse = sunColor.rgb * sunColor.a * diffuseFactor;

    vec3 finalColor = albedo * (ambient + diffuse);

    color = vec4(finalColor, 1.0);
}