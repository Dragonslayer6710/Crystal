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
    public void update(SceneUpdateContext context) {
        float dt = (float) context.getDeltaTime();

        getOwner().getTransform().rotate(
                xRadiansPerSecond * (float) dt,
                yRadiansPerSecond * (float) dt,
                zRadiansPerSecond * (float) dt
        );
    }

    public RotationComponent setSpeed(float xRadiansPerSecond, float yRadiansPerSecond, float zRadiansPerSecond) {
        this.xRadiansPerSecond = xRadiansPerSecond;
        this.yRadiansPerSecond = yRadiansPerSecond;
        this.zRadiansPerSecond = zRadiansPerSecond;
        return this;
    }
}
