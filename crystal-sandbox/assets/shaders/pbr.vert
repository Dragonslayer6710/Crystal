#version 460 core

layout (location = 0) in vec3 a_Position;
layout (location = 1) in vec2 a_TexCoord;
layout (location = 2) in vec3 a_Normal;
layout (location = 3) in vec3 a_Tangent;

uniform mat4 model;

layout (std140, binding = 0) uniform SceneData {
    mat4 view;
    mat4 projection;

    vec4 ambient;        // rgb = color, a = intensity
    vec4 cameraPosition; // xyz = world position

    vec4 sunDirection;   // xyz = direction
    vec4 sunColor;       // rgb = color, a = intensity

    mat4 lightSpaceMatrix;
};

out vec3 v_WorldPosition;
out vec2 v_UV;
out vec3 v_Normal;
out vec3 v_Tangent;
out vec4 v_LightSpacePosition;

void main() {
    vec4 worldPosition = model * vec4(a_Position, 1.0);

    v_WorldPosition = worldPosition.xyz;
    v_UV = a_TexCoord;

    mat3 normalMatrix = mat3(transpose(inverse(model)));

    v_Normal = normalMatrix * a_Normal;
    v_Tangent = normalMatrix * a_Tangent;

    v_LightSpacePosition = lightSpaceMatrix * model * vec4(a_Position, 1.0);

    gl_Position = projection * view * worldPosition;
}