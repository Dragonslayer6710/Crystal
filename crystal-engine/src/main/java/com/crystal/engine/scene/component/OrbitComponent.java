package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;
import org.joml.Math;
import org.joml.Vector3f;

public class OrbitComponent extends SceneComponent {

    private final Vector3f center = new Vector3f();

    private float radius = 1.0f;
    private float speed = 1.0f;
    private float phase;
    private float elapsedTime;

    public OrbitComponent() {}

    public OrbitComponent(float radius, float speed) {
        setRadius(radius);
        setSpeed(speed);
    }

    public Vector3f getCenter() {
        return new Vector3f(center);
    }

    public float getRadius() {
        return radius;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPhase() {
        return phase;
    }

    public OrbitComponent setCenter(float x, float y, float z) {
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z))
            throw new IllegalArgumentException("Center values must be finite");

        center.set(x, y, z);
        return this;
    }

    public OrbitComponent setRadius(float radius) {
        if (!Float.isFinite(radius) || radius < 0.0f)
            throw new IllegalArgumentException("Radius must be finite and non-negative");

        this.radius = radius;
        return this;
    }

    public OrbitComponent setSpeed(float speed) {
        if (!Float.isFinite(speed))
            throw new IllegalArgumentException("Speed must be finite");

        this.speed = speed;
        return this;
    }

    public OrbitComponent setPhase(float phase) {
        if (!Float.isFinite(phase))
            throw new IllegalArgumentException("Phase must be finite");

        this.phase = phase;
        return this;
    }

    @Override
    public void update(SceneUpdateContext context) {
        if (context == null) throw new IllegalArgumentException("SceneUpdateContext cannot be null");

        elapsedTime += (float) context.getDeltaTime();

        float angle = elapsedTime * speed + phase;
        float x = center.x + (float) Math.cos(angle) * radius;
        float z = center.z + (float) Math.sin(angle) * radius;

        getOwner().getTransform().setPosition(
            x,
            center.y,
            z
        );
    }
}
