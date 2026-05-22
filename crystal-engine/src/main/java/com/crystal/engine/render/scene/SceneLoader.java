package com.crystal.engine.render.scene;

import com.crystal.engine.assets.model.Model;
import com.crystal.engine.assets.model.ModelLoadOptions;
import com.crystal.engine.core.ResourceManager;
import com.crystal.engine.render.environment.IBLGenerator;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.render.mesh.MeshFactory;
import com.crystal.engine.render.shader.Shader;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SceneLoader {

    private SceneLoader() {}

    public static void load(Path scenePath, Scene scene, ResourceManager resources, Shader shader) {
        if (scenePath == null) throw new IllegalArgumentException("Scene path cannot be null");
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");
        if (shader == null) throw new IllegalArgumentException("Shader cannot be null");

        SceneDefinition definition = read(scenePath);

        applyCamera(definition.camera, scene);
        applyLighting(definition.lighting, scene);
        applyEnvironment(definition.environment, scene, resources);
        applyObjects(definition.objects, scene, resources, shader);
    }

    private static SceneDefinition read(Path scenePath) {
        try {
            return new ObjectMapper().readValue(scenePath.toFile(), SceneDefinition.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load scene: " + scenePath, e);
        }
    }

    private static void applyCamera(CameraDefinition camera, Scene scene) {
        if (camera == null || camera.position == null)
            return;

        float[] position = vec3(camera.position, "camera.position");
        scene.getCamera().getTransform().setPosition(position[0], position[1], position[2]);
    }

    private static void applyLighting(LightingDefinition lighting, Scene scene) {
        if (lighting == null)
            return;

        if (lighting.directionalIntensity != null)
            scene.getDirectionalLight().setIntensity(lighting.directionalIntensity);

        if (lighting.shadowStrength != null)
            scene.getDirectionalLight().setShadowStrength(lighting.shadowStrength);
    }

    private static void applyEnvironment(EnvironmentDefinition environment, Scene scene, ResourceManager resources) {
        if (environment == null)
            return;

        if (environment.ambientColor != null) {
            float[] color = vec3(environment.ambientColor, "environment.ambientColor");
            scene.getEnvironment().setAmbientColor(color[0], color[1], color[2]);
        }

        if (environment.ambientIntensity != null)
            scene.getEnvironment().setAmbientIntensity(environment.ambientIntensity);

        if (environment.iblIntensity != null)
            scene.getEnvironment().setIblIntensity(environment.iblIntensity);

        if (environment.ibl != null && !environment.ibl.isBlank()) {
            IBLGenerator iblGenerator = IBLGenerator.createDefault(resources);
            iblGenerator.generateFromHDR(scene.getEnvironment(), environment.ibl);
        }
    }

    private static void applyObjects(List<ObjectDefinition> objects, Scene scene, ResourceManager resources, Shader shader) {
        if (objects == null)
            return;

        for (ObjectDefinition object : objects) {
            SceneObject sceneObject = createSceneObject(object, resources, shader);

            applyTransform(object, sceneObject.getTransform());
            applyComponents(object, sceneObject);
            applyChildren(object, sceneObject, resources, shader);

            if (object.castsShadow != null)
                sceneObject.setCastsShadowRecursive(object.castsShadow);

            scene.add(sceneObject);
        }
    }

    private static void applyTransform(ObjectDefinition object, Transform transform) {
        if (object.position != null) {
            float[] position = vec3(object.position, object.name + ".position");
            transform.setPosition(position[0], position[1], position[2]);
        }

        if (object.rotationDegrees != null) {
            float[] rotation = vec3(object.rotationDegrees, object.name + ".rotationDegrees");
            transform.setRotationDegrees(rotation[0], rotation[1], rotation[2]);
        }

        if (object.scale != null) {
            float[] scale = vec3(object.scale, object.name + ".scale");
            transform.setScale(scale[0], scale[1], scale[2]);
        }
    }

    private static void applyComponents(ObjectDefinition object, SceneObject sceneObject) {
        if (object.components == null)
            return;

        for (ComponentDefinition component : object.components) {
            switch (component.type) {
                case "rotation" -> {
                    float[] speed = vec3(component.speedRadiansPerSecond, object.name + ".rotation.speedRadiansPerSecond");
                    sceneObject.addComponent(new RotationComponent(speed[0], speed[1], speed[2]));
                }
                default -> throw new IllegalArgumentException("Unsupported component type: " + component.type);
            }
        }
    }

    private static void applyChildren(ObjectDefinition object, SceneObject parent, ResourceManager resources, Shader shader) {
        if (object.children == null)
            return;

        for (ObjectDefinition childDefinition : object.children) {
            SceneObject child = createSceneObject(childDefinition, resources, shader);
            applyTransform(childDefinition, child.getTransform());
            applyComponents(childDefinition, child);
            applyChildren(childDefinition, child, resources, shader);

            if (childDefinition.castsShadow != null)
                child.setCastsShadowRecursive(childDefinition.castsShadow);

            parent.addChild(child);
        }
    }

    private static SceneObject loadModelObject(ObjectDefinition object, ResourceManager resources, Shader shader) {
        if (object.path == null || object.path.isBlank())
            throw new IllegalArgumentException("Model object path cannot be null or blank");
        Model model = resources.loadModel(object.path, new ModelLoadOptions().setShader(shader));

        SceneObject root = model.getRootObjects().getFirst();

        if (object.name != null)
            root.setName(object.name);

        return root;
    }

    private static SceneObject createPrimitiveObject(ObjectDefinition object, ResourceManager resources, Shader shader) {
        if (object.primitive == null || object.primitive.isBlank())
            throw new IllegalArgumentException("Primitive object must define primitive");

        Mesh mesh = switch (object.primitive) {
            case "plane" -> MeshFactory.createLitTexturedPlane(resources);
            case "cube" -> MeshFactory.createLitTexturedCube(resources);
            default -> throw new IllegalArgumentException("Unsupported primitive: " + object.primitive);
        };

        Material material = new Material(shader);

        if (object.material != null) {
            if (object.material.albedo != null)
                material.setAlbedo(resources.createTexture(object.material.albedo));

            if (object.material.normal != null)
                material.setNormalMap(resources.createDataTexture(object.material.normal));
        }

        return new SceneObject(object.name, mesh, material, new Transform());
    }

    private static SceneObject createSceneObject(ObjectDefinition object, ResourceManager resources, Shader shader) {
        return switch (object.type) {
            case "model" -> loadModelObject(object, resources, shader);
            case "primitive" -> createPrimitiveObject(object, resources, shader);
            default -> throw new IllegalArgumentException("Unsupported scene object type: " + object.type);
        };
    }

    private static float[] vec3(List<Float> values, String fieldName) {
        if (values.size() != 3) throw new IllegalArgumentException(fieldName + " must contain exactly 3 values");

        return new float[] { values.get(0), values.get(1), values.get(2) };
    }

    private static final class SceneDefinition {
        public CameraDefinition camera;
        public EnvironmentDefinition environment;
        public LightingDefinition lighting;
        public List<ObjectDefinition> objects;
    }

    private static final class CameraDefinition {
        public List<Float> position;
    }

    private static final class EnvironmentDefinition {
        public List<Float> ambientColor;
        public Float ambientIntensity;
        public String ibl;
        public Float iblIntensity;
    }

    private static final class LightingDefinition {
        public Float directionalIntensity;
        public Float shadowStrength;
    }

    private static final class ObjectDefinition {
        public String name;
        public String type;
        public String path;
        public String primitive;
        public List<Float> position;
        public List<Float> rotationDegrees;
        public List<Float> scale;
        public Boolean castsShadow;
        public MaterialDefinition material;
        public List<ComponentDefinition> components;
        public List<ObjectDefinition> children;
    }

    private static final class ComponentDefinition {
        public String type;
        public List<Float> speedRadiansPerSecond;
    }

    private static final class MaterialDefinition {
        public String albedo;
        public String normal;
    }
}
