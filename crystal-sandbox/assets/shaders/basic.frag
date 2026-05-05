#version 460 core

in vec3 v_FragColor;
out vec4 color;

void main() {
    color = vec4(v_FragColor, 1);
}