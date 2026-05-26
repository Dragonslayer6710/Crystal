package com.crystal.engine.scene.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Frustum {

    private final Vector4f[] planes = {
            new Vector4f(),
            new Vector4f(),
            new Vector4f(),
            new Vector4f(),
            new Vector4f(),
            new Vector4f()
    };

    public void update(Matrix4f viewProjection) {
        // Calculate Planes
        {// Left
            planes[0].set(
                    viewProjection.m03() + viewProjection.m00(),
                    viewProjection.m13() + viewProjection.m10(),
                    viewProjection.m23() + viewProjection.m20(),
                    viewProjection.m33() + viewProjection.m30()
            );

            // Right
            planes[1].set(
                    viewProjection.m03() - viewProjection.m00(),
                    viewProjection.m13() - viewProjection.m10(),
                    viewProjection.m23() - viewProjection.m20(),
                    viewProjection.m33() - viewProjection.m30()
            );

            // Bottom
            planes[2].set(
                    viewProjection.m03() + viewProjection.m01(),
                    viewProjection.m13() + viewProjection.m11(),
                    viewProjection.m23() + viewProjection.m21(),
                    viewProjection.m33() + viewProjection.m31()
            );

            // Top
            planes[3].set(
                    viewProjection.m03() - viewProjection.m01(),
                    viewProjection.m13() - viewProjection.m11(),
                    viewProjection.m23() - viewProjection.m21(),
                    viewProjection.m33() - viewProjection.m31()
            );

            // Near
            planes[4].set(
                    viewProjection.m03() + viewProjection.m02(),
                    viewProjection.m13() + viewProjection.m12(),
                    viewProjection.m23() + viewProjection.m22(),
                    viewProjection.m33() + viewProjection.m32()
            );

            // Far
            planes[5].set(
                    viewProjection.m03() - viewProjection.m02(),
                    viewProjection.m13() - viewProjection.m12(),
                    viewProjection.m23() - viewProjection.m22(),
                    viewProjection.m33() - viewProjection.m32()
            );
        }

        for (Vector4f plane : planes) {
            normalizePlane(plane);
        }
    }

    public boolean containsSphere(Vector3f center, float radius) {
        for (Vector4f plane : planes) {
            float distance =
                    plane.x * center.x +
                    plane.y * center.y +
                    plane.z * center.z +
                    plane.w;

            if (distance < -radius)
                return false;
        }

        return true;
    }

    private void normalizePlane(Vector4f plane) {
        float length = (float) Math.sqrt(
                plane.x * plane.x +
                plane.y * plane.y +
                plane.z * plane.z
        );

        plane.div(length);
    }
}
