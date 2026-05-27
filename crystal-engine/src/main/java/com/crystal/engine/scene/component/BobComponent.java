package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;
import org.joml.Math;
import org.joml.Vector3f;

public class BobComponent extends SceneComponent {

    private final Vector3f origin = new Vector3f();

    private float amplitude = 0.25f;
    private float speed = 1.0f;
    private float phase;
    private float elapsedTime;
    private boolean originCaptured;

    public BobComponent() {}

    public BobComponent(float amplitude, float speed) {
        setAmplitude(amplitude);
        setSpeed(speed);
    }

    public float getAmplitude() {
        return amplitude;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPhase() {
        return phase;
    }

    public BobComponent setAmplitude(float amplitude) {
        if (!Float.isFinite(amplitude) || amplitude < 0.0f)
            throw new IllegalArgumentException("Amplitude must be finite and non-negative");

        this.amplitude = amplitude;
        return this;
    }

    public BobComponent setSpeed(float speed) {
        if (!Float.isFinite(speed))
            throw new IllegalArgumentException("Speed must be finite");

        this.speed = speed;
        return this;
    }

    public BobComponent setPhase(float phase) {
        if (!Float.isFinite(phase))
            throw new IllegalArgumentException("Phase must be finite");

        this.phase = phase;
        return this;
    }

    @Override
    public void update(SceneUpdateContext context) {
        if (context == null) throw new IllegalArgumentException("SceneUpdateContext cannot be null");

        if (!originCaptured) {
            origin.set(getOwner().getTransform().getPosition());
            originCaptured = true;
        }

        elapsedTime += (float) context.getDeltaTime();

        float yOffset = (float) Math.sin(elapsedTime * speed + phase) * amplitude;

        getOwner().getTransform().setPosition(
            origin.x,
            origin.y + yOffset,
            origin.z
        );
    }
}
