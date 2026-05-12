#version 460 core

in vec2 v_UV;

out vec4 f_Color;

void main() {
    f_Color = vec4(v_UV, 0.0, 1.0);
}