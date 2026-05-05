#version 460 core

layout (location = 0) in vec3 a_Position;
layout (location = 1) in vec3 a_Color;
layout (location = 2) in vec2 a_TexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 v_FragColor;
out vec2 v_TexCoord;

void main() {
    v_FragColor = a_Color;
    v_TexCoord = a_TexCoord;

    gl_Position = projection * view * model * vec4(a_Position, 1.0);
}