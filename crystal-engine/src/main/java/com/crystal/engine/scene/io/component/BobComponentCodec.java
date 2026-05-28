package com.crystal.engine.scene.io.component;

import com.crystal.engine.scene.component.BobComponent;

import static com.crystal.engine.scene.io.SceneDefinition.ComponentDefinition;

final class BobComponentCodec implements SceneComponentCodec<BobComponent> {

    @Override
    public String type() {
        return "bob";
    }

    @Override
    public Class<BobComponent> componentClass() {
        return BobComponent.class;
    }

    @Override
    public BobComponent read(String objectName, ComponentDefinition definition) {
        BobComponent bob = new BobComponent();

        if (definition.amplitude != null)
            bob.setAmplitude(definition.amplitude);

        if (definition.speed != null)
            bob.setSpeed(definition.speed);

        if (definition.phase != null)
            bob.setPhase(definition.phase);

        return bob;
    }

    @Override
    public ComponentDefinition write(BobComponent component) {
        ComponentDefinition definition = new ComponentDefinition();
        definition.type = type();
        definition.amplitude = component.getAmplitude();
        definition.speed = component.getSpeed();
        definition.phase = component.getPhase();

        return definition;
    }
}
