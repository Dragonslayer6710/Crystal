package com.crystal.engine.scene.io;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;

public final class SceneDefinitionValues {

    private SceneDefinitionValues() {}

    public static float[] requiredVec3(List<Float> values, String fieldName) {
        if (values == null)
            throw new IllegalArgumentException(fieldName + " must be defined");

        if (values.size() != 3)
            throw new IllegalArgumentException(fieldName + " must contain exactly 3 values");

        return new float[] { values.get(0), values.get(1), values.get(2) };
    }

    public static Vector3f optionalVec3(List<Float> values, String fieldName) {
        if (values == null)
            return null;

        float[] vector = requiredVec3(values, fieldName);
        return new Vector3f(vector[0], vector[1], vector[2]);
    }

    public static List<Float> vec3(Vector3fc value) {
        return List.of(value.x(), value.y(), value.z());
    }

    public static List<Float> vec3Degrees(Vector3fc radians) {
        return List.of(
            (float) Math.toDegrees(radians.x()),
            (float) Math.toDegrees(radians.y()),
            (float) Math.toDegrees(radians.z())
        );
    }

    public static boolean isZero(Vector3fc value) {
        return value.x() == 0.0f && value.y() == 0.0f && value.z() == 0.0f;
    }

    public static boolean isOne(Vector3fc value) {
        return value.x() == 1.0f && value.y() == 1.0f && value.z() == 1.0f;
    }
}
