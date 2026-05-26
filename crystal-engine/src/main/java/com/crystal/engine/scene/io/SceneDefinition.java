package com.crystal.engine.scene.io;

import java.util.List;

public final class SceneDefinition {
    public String name;
    public Integer version;
    public CameraDefinition camera;
    public EnvironmentDefinition environment;
    public LightingDefinition lighting;
    public List<MaterialDefinition> materials;
    public List<ObjectDefinition> objects;

    public static final class CameraDefinition {
        public List<Float> position;
    }

    public static final class EnvironmentDefinition {
        public List<Float> ambientColor;
        public Float ambientIntensity;
        public String ibl;
        public Float iblDiffuseIntensity;
        public Float iblSpecularIntensity;
    }

    public static final class LightingDefinition {
        public Float directionalIntensity;
        public Float shadowStrength;
    }

    public static final class ObjectDefinition {
        public String name;
        public String type;
        public String path;
        public String primitive;
        public List<Float> position;
        public List<Float> rotationDegrees;
        public List<Float> scale;
        public List<String> tags;
        public List<String> layers;
        public Integer layerMask;
        public Boolean castsShadow;
        public String material;
        public List<ComponentDefinition> components;
        public List<ObjectDefinition> children;
        public TriggerDefinition trigger;
    }

    public static final class TriggerDefinition {
        public List<Float> halfExtents;
    }

    public static final class ComponentDefinition {
        public String type;
        public List<Float> speedRadiansPerSecond;
        public Boolean loop;
        public List<KeyframeDefinition> keyframes;
    }

    public static final class KeyframeDefinition {
        public double time;
        public List<Float> position;
        public List<Float> rotationDegrees;
        public List<Float> scale;
    }

    public static final class MaterialDefinition {
        public String name;
        public String albedo;
        public String normal;
        public Float roughness;
        public Float metallic;
        public Float normalStrength;
    }
}
