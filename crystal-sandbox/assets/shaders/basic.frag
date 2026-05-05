#version 460 core

in vec3 v_Color;
in vec2 v_UV;
in vec3 v_Normal;

uniform sampler2D albedoTexture;

out vec4 color;

void main() {
    vec3 lightDir = normalize(vec3(1.0, 1.0, 0.5));
    float diffuse = max(dot(normalize(v_Normal), lightDir), 0.0);

    vec3 texColor = texture(albedoTexture, v_UV).rgb * v_Color;

    color = vec4(texColor * diffuse, 1.0);
}