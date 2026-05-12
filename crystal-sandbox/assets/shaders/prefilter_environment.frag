#version 460 core

in vec3 v_LocalPosition;

uniform samplerCube environmentMap;
uniform float roughness;

out vec4 f_Color;

void main() {
    vec3 N = normalize(v_LocalPosition);
    vec3 R = N;

    vec3 prefilteredColor =
            textureLod(environmentMap, R, roughness * 4.0).rgb;

    f_Color = vec4(prefilteredColor, 1.0);
}