package com.crystal.engine.render.scene;

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
    public void update(double deltaTime) {
        getOwner().getTransform().rotate(
                xRadiansPerSecond * (float) deltaTime,
                yRadiansPerSecond * (float) deltaTime,
                zRadiansPerSecond * (float) deltaTime
        );
    }

    public RotationComponent setSpeed(float xRadiansPerSecond, float yRadiansPerSecond, float zRadiansPerSecond) {
        this.xRadiansPerSecond = xRadiansPerSecond;
        this.yRadiansPerSecond = yRadiansPerSecond;
        this.zRadiansPerSecond = zRadiansPerSecond;
        return this;
    }
}
