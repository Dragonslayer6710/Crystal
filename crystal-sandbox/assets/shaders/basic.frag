#version 460 core

in vec3 v_FragColor;
in vec2 v_TexCoord;
out vec4 color;

void main() {
    color = vec4(v_TexCoord, 0.0, 1.0);
}