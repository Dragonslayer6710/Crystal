#version 460 core

in vec3 v_LocalPosition;

uniform sampler2D equirectangularMap;

out vec4 f_Color;

const vec2 INV_ATAN = vec2(0.1591, 0.3183);

vec2 sampleSphericalMap(vec3 direction) {
    vec2 uv = vec2(atan(direction.z, direction.x), asin(direction.y));
    uv *= INV_ATAN;
    uv += 0.5;
    return uv;
}

void main() {
    vec3 direction = normalize(v_LocalPosition);
    vec2 uv = sampleSphericalMap(direction);
    vec3 envColor = texture(equirectangularMap, uv).rgb;

    f_Color = vec4(envColor, 1.0);
}
