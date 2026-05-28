#version 460 core

layout (location = 0) in vec3 aPosition;

uniform mat4 model;

layout (std140, binding = 0) uniform SceneData {
    mat4 view;
    mat4 projection;

    vec4 ambient;
    vec4 cameraPosition;

    vec4 sunDirection;
    vec4 sunColor;

    mat4 lightSpaceMatrix;

    vec4 pointLightCount;
    vec4 pointLights[16];
};

void main() {
    gl_Position = projection * view * model * vec4(aPosition, 1.0);
}
