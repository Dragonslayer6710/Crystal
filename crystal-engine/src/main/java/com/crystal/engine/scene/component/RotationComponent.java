package com.crystal.engine.scene.component;

import com.crystal.engine.scene.SceneComponent;
import com.crystal.engine.scene.SceneUpdateContext;

public class RotationComponent extends SceneComponent {

    private float xRadiansPerSecond;
    private float yRadiansPerSecond;
    private float zRadiansPerSecond;

    public RotationComponent(float xRadiansPerSecond, float yRadiansPerSecond, float zRadiansPerSecond) {
        this.xRadiansPerSecond = xRadiansPerSecond;
        this.yRadiansPerSecond = yRadiansPerSecond;
        this.zRadiansPerSecond = zRadiansPerSecond;
    }

    @Override
    public void update(SceneUpdateContext context) {
        if (context == null) throw new IllegalArgumentException("SceneUpdateContext cannot be null");

        float dt = (float) context.getDeltaTime();

        getOwner().getTransform().rotate(
                xRadiansPerSecond * (float) dt,
                yRadiansPerSecond * (float) dt,
                zRadiansPerSecond * (float) dt
        );
    }

    public float getXRadiansPerSecond() {
        return xRadiansPerSecond;
    }

    public float getYRadiansPerSecond() {
        return yRadiansPerSecond;
    }

    public float getZRadiansPerSecond() {
        return zRadiansPerSecond;
    }

    public RotationComponent setSpeed(float xRadiansPerSecond, float yRadiansPerSecond, float zRadiansPerSecond) {
        this.xRadiansPerSecond = xRadiansPerSecond;
        this.yRadiansPerSecond = yRadiansPerSecond;
        this.zRadiansPerSecond = zRadiansPerSecond;
        return this;
    }
}
