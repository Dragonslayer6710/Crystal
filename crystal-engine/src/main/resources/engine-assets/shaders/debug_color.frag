#version 460 core

uniform vec3 debugColor;

out vec4 f_Color;

void main() {
    f_Color = vec4(debugColor, 1.0);
}
