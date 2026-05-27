package com.crystal.engine.render.uniform;

import com.crystal.engine.scene.Scene;

public final class SceneUniformData {

    public static final int BINDING_POINT = UniformBindings.SCENE;
    public static final int MAX_POINT_LIGHTS = 8;

    public static final int FLOAT_COUNT = 64 + 4 + MAX_POINT_LIGHTS * 8;
    public static final int BYTE_SIZE = FLOAT_COUNT * Float.BYTES;

    /*
     * Must match the SceneData uniform block layout in GLSL.
     *
     * layout(std140, binding = 0) uniform SceneData {
     *     mat4 view;                    // offset 0   / floats 0-15
     *     mat4 projection;              // offset 64  / floats 16-31
     *     vec4 ambient;                 // offset 128 / floats 32-35
     *     vec4 cameraPosition;          // offset 144 / floats 36-39
     *     vec4 sunDirection;            // offset 160 / floats 40-43
     *     vec4 sunColor;                // offset 176 / floats 44-47
     *     mat4 lightSpaceMatrix;        // offset 192 / floats 48-63
     *     vec4 pointLightCount;         // offset 256 / floats 64-67
     *     PointLight pointLights[8];    // offset 272 / floats 68-131
     * };
     *
     * PointLight:
     *     vec4 positionRadius;          // xyz = position, w = radius
     *     vec4 colorIntensity;          // rgb = color, w = intensity
     */

    private static final int VIEW_MATRIX_OFFSET = 0;
    private static final int PROJECTION_MATRIX_OFFSET = 16;
    private static final int AMBIENT_OFFSET = 32;
    private static final int CAMERA_POSITION_OFFSET = 36;
    private static final int LIGHT_DIRECTION_OFFSET = 40;
    private static final int LIGHT_COLOR_OFFSET = 44;
    private static final int LIGHT_SPACE_MATRIX_OFFSET = 48;
    private static final int POINT_LIGHT_COUNT_OFFSET = 64;
    private static final int POINT_LIGHTS_OFFSET = 68;

    private static final int POINT_LIGHT_STRIDE = 8;

    private final float[] data = new float[FLOAT_COUNT];

    public float[] from(Scene scene, float aspectRatio) {
        var camera = scene.getCamera();

        camera.getViewMatrix().get(data, VIEW_MATRIX_OFFSET);
        camera.getProjectionMatrix(aspectRatio).get(data, PROJECTION_MATRIX_OFFSET);

        var environment = scene.getEnvironment();
        var ambientColor = environment.getAmbientColor();

        data[AMBIENT_OFFSET] = ambientColor.x;
        data[AMBIENT_OFFSET + 1] = ambientColor.y;
        data[AMBIENT_OFFSET + 2] = ambientColor.z;
        data[AMBIENT_OFFSET + 3] = environment.getAmbientIntensity();

        var cameraPosition = camera.getTransform().getWorldPosition();

        data[CAMERA_POSITION_OFFSET] = cameraPosition.x;
        data[CAMERA_POSITION_OFFSET + 1] = cameraPosition.y;
        data[CAMERA_POSITION_OFFSET + 2] = cameraPosition.z;
        data[CAMERA_POSITION_OFFSET + 3] = 0.0f;

        var light = scene.getDirectionalLight();
        var lightDirection = light.getDirection();

        data[LIGHT_DIRECTION_OFFSET] = lightDirection.x;
        data[LIGHT_DIRECTION_OFFSET + 1] = lightDirection.y;
        data[LIGHT_DIRECTION_OFFSET + 2] = lightDirection.z;
        data[LIGHT_DIRECTION_OFFSET + 3] = 0.0f;

        var lightColor = light.getColor();

        data[LIGHT_COLOR_OFFSET] = lightColor.x;
        data[LIGHT_COLOR_OFFSET + 1] = lightColor.y;
        data[LIGHT_COLOR_OFFSET + 2] = lightColor.z;
        data[LIGHT_COLOR_OFFSET + 3] = light.getIntensity();

        light.getLightSpaceMatrix().get(data, LIGHT_SPACE_MATRIX_OFFSET);

        var pointLights = scene.getPointLights();
        int pointLightCount = Math.min(pointLights.size(), MAX_POINT_LIGHTS);

        data[POINT_LIGHT_COUNT_OFFSET] = pointLightCount;
        data[POINT_LIGHT_COUNT_OFFSET + 1] = 0.0f;
        data[POINT_LIGHT_COUNT_OFFSET + 2] = 0.0f;
        data[POINT_LIGHT_COUNT_OFFSET + 3] = 0.0f;

        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            int offset = POINT_LIGHTS_OFFSET + i * POINT_LIGHT_STRIDE;

            if (i >= pointLightCount) {
                for (int j = 0; j < POINT_LIGHT_STRIDE; j++)
                    data[offset + j] = 0.0f;

                continue;
            }

            var pointLight = pointLights.get(i).getLight();
            var position = pointLight.getPosition();
            var color = pointLight.getColor();

            data[offset] = position.x;
            data[offset + 1] = position.y;
            data[offset + 2] = position.z;
            data[offset + 3] = pointLight.getRadius();

            data[offset + 4] = color.x;
            data[offset + 5] = color.y;
            data[offset + 6] = color.z;
            data[offset + 7] = pointLight.getIntensity();
        }

        return data;
    }
}
