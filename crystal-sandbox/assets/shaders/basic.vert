#version 460 core

layout (location = 0) in vec3 a_Position;
layout (location = 1) in vec3 a_Color;
layout (location = 2) in vec2 a_TexCoord;
layout (location = 3) in vec3 a_Normal;

uniform mat4 model;

layout (std140, binding = 0) uniform SceneData {
    mat4 view;
    mat4 projection;
    vec4 ambient;
    vec4 sunDirection;
    vec4 sunColor;
};

out vec3 v_Color;
out vec2 v_UV;
out vec3 v_Normal;

void main() {
    v_Color = a_Color;
    v_UV = a_TexCoord;

    // IMPORTANT: transform normal into world space
    v_Normal = mat3(transpose(inverse(model))) * a_Normal;

    gl_Position = projection * view * model * vec4(a_Position, 1.0);
}