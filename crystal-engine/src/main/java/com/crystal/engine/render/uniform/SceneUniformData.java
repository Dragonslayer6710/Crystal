package com.crystal.engine.render.uniform;

import com.crystal.engine.render.scene.Scene;

public final class SceneUniformData {

    public static final int BINDING_POINT = UniformBindings.SCENE;
    public static final int FLOAT_COUNT = 48;
    public static final int BYTE_SIZE = FLOAT_COUNT * Float.BYTES;

    /*
     * Must match the Scene uniform block layout in GLSL.
     *
     * layout(std140, binding = 0) uniform Scene {
     *     mat4 u_View;              // offset 0   / floats 0-15
     *     mat4 u_Projection;        // offset 64  / floats 16-31
     *     vec4 u_Ambient;           // offset 128 / floats 32-35
     *     vec4 u_CameraPosition;    // offset 144 / floats 36-39
     *     vec4 u_LightDirection;    // offset 160 / floats 40-43
     *     vec4 u_LightColor;        // offset 176 / floats 44-47
     * };
     */

    private static final int VIEW_MATRIX_OFFSET = 0;
    private static final int PROJECTION_MATRIX_OFFSET = 16;
    private static final int AMBIENT_OFFSET = 32;
    private static final int CAMERA_POSITION_OFFSET = 36;
    private static final int LIGHT_DIRECTION_OFFSET = 40;
    private static final int LIGHT_COLOR_OFFSET = 44;

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

        return data;
    }
}
