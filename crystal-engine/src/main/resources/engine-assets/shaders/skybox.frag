#version 460 core

in vec3 vTexCoords;

out vec4 fragColor;

uniform samplerCube skybox;

void main() {
    fragColor = texture(skybox, vTexCoords);
}
