#version 460 core

in vec3 v_FragColor;
in vec2 v_TexCoord;

uniform sampler2D albedoTexture;

out vec4 color;

void main() {
    color = texture(albedoTexture, v_TexCoord) * vec4(v_FragColor, 1.0);
}