#version 460 core

out vec2 v_UV;

const vec2 POSITIONS[3] = vec2[](
    vec2(-1.0, -1.0),
    vec2( 3.0, -1.0),
    vec2(-1.0,  3.0)
);

void main() {
    vec2 position = POSITIONS[gl_VertexID];
    v_UV = position * 0.5 + 0.5;
    gl_Position = vec4(position, 0.0, 1.0);
}
