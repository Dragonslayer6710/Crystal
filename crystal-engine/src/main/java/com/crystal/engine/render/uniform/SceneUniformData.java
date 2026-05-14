package com.crystal.engine.render.uniform;

import com.crystal.engine.render.scene.Scene;

public class SceneUniformData {

    public static final int FLOAT_COUNT = 48;
    public static final int BYTE_SIZE = FLOAT_COUNT * Float.BYTES;

    private SceneUniformData() {}

    public static float[] from(Scene scene, float aspectRatio) {
        float[] data = new float[FLOAT_COUNT];

        var camera = scene.getCamera();

        camera.getViewMatrix().get(data, 0);
        camera.getProjectionMatrix(aspectRatio).get(data, 16);

        var environment = scene.getEnvironment();
        var ambientColor = environment.getAmbientColor();

        data[32] = ambientColor.x;
        data[33] = ambientColor.y;
        data[34] = ambientColor.z;
        data[35] = environment.getAmbientIntensity();

        var cameraPosition = camera.getTransform().getWorldPosition();

        data[36] = cameraPosition.x;
        data[37] = cameraPosition.y;
        data[38] = cameraPosition.z;
        data[39] = 0.0f;

        var light = scene.getDirectionalLight();
        var lightDirection = light.getDirection();

        data[40] = lightDirection.x;
        data[41] = lightDirection.y;
        data[42] = lightDirection.z;
        data[43] = 0.0f;

        var lightColor = light.getColor();

        data[44] = lightColor.x;
        data[45] = lightColor.y;
        data[46] = lightColor.z;
        data[47] = light.getIntensity();

        return data;
    }
}
