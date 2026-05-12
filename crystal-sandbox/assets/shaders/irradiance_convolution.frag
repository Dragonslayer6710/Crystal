#version 460 core

in vec3 v_LocalPosition;

uniform samplerCube environmentMap;

out vec4 f_Color;

void main() {
    vec3 direction = normalize(v_LocalPosition);
    vec3 envColor = texture(environmentMap, direction).rgb;

    f_Color = vec4(envColor, 1.0);
}