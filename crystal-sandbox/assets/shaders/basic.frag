#version 460 core

in vec3 v_Color;
in vec2 v_UV;
in vec3 v_Normal;

uniform sampler2D albedoTexture;

struct DirectionalLight {
    vec3 direction;
    vec3 color;
    float intensity;
};

uniform DirectionalLight sun;

uniform vec3 ambientColor;
uniform float ambientIntensity;

out vec4 color;

void main() {
    vec3 normal = normalize(v_Normal);

    vec3 lightDir = normalize(-sun.direction);

    float diffuseFactor = max(dot(normal, lightDir), 0.0);

    vec3 albedo = texture(albedoTexture, v_UV).rgb * v_Color;

    vec3 ambient = ambientColor * ambientIntensity;
    vec3 diffuse = sun.color * sun.intensity * diffuseFactor;

    vec3 finalColor = albedo * (ambient + diffuse);

    color = vec4(finalColor, 1.0);
}