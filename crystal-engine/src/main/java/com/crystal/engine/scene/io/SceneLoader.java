package com.crystal.engine.scene.io;

import com.crystal.engine.assets.model.Model;
import com.crystal.engine.assets.model.ModelLoadOptions;
import com.crystal.engine.assets.ResourceManager;
import com.crystal.engine.render.RenderLayers;
import com.crystal.engine.render.environment.Environment;
import com.crystal.engine.render.material.Material;
import com.crystal.engine.render.mesh.Mesh;
import com.crystal.engine.scene.Scene;
import com.crystal.engine.scene.SceneObject;
import com.crystal.engine.scene.SceneObjectSource;
import com.crystal.engine.scene.Transform;
import com.crystal.engine.scene.animation.KeyframeAnimationComponent;
import com.crystal.engine.scene.animation.TransformKeyframe;
import com.crystal.engine.scene.collision.TriggerVolume;
import com.crystal.engine.scene.component.RotationComponent;
import com.crystal.engine.render.shader.Shader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.crystal.engine.scene.io.SceneDefinition.*;

public class SceneLoader {

    private SceneLoader() {}

    public static LoadedScene loadInto(Path scenePath, Scene scene, ResourceManager resources, Shader shader) {
        if (scenePath == null) throw new IllegalArgumentException("Scene path cannot be null");
        if (scene == null) throw new IllegalArgumentException("Scene cannot be null");
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");
        if (shader == null) throw new IllegalArgumentException("Shader cannot be null");

        SceneDefinition definition = read(scenePath);

        apply(definition, scene, resources, shader);

        return new LoadedScene(
            scene,
            sceneName(definition, scenePath),
            sceneVersion(definition)
        );
    }

    public static LoadedScene loadNew(Path scenePath, ResourceManager resources, Shader shader) {
        if (scenePath == null) throw new IllegalArgumentException("Scene path cannot be null");
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");
        if (shader == null) throw new IllegalArgumentException("Shader cannot be null");

        SceneDefinition definition = read(scenePath);
        Scene scene = new Scene();

        apply(definition, scene, resources, shader);

        return new LoadedScene(
            scene,
            sceneName(definition, scenePath),
            sceneVersion(definition)
        );
    }

    public record LoadedScene(Scene scene, String name, int version) {}

    private static SceneDefinition read(Path scenePath) {
        try {
            return new ObjectMapper().readValue(scenePath.toFile(), SceneDefinition.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load scene: " + scenePath, e);
        }
    }

    private static void apply(SceneDefinition definition, Scene scene, ResourceManager resources, Shader shader) {
        applyCamera(definition.camera, scene);
        applyLighting(definition.lighting, scene);
        applyEnvironment(definition.environment, scene, resources);

        Map<String, Material> materials = createMaterials(definition.materials, resources, shader);
        applyObjects(definition.objects, scene, resources, shader, materials);
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

        if (environment.iblDiffuseIntensity != null)
            scene.getEnvironment().setIblDiffuseIntensity(environment.iblDiffuseIntensity);

        if (environment.iblSpecularIntensity != null)
            scene.getEnvironment().setIblSpecularIntensity(environment.iblSpecularIntensity);

        if (environment.ibl != null && !environment.ibl.isBlank()) {
            Environment cachedEnvironment = resources.getOrCreateIBLEnvironment(environment.ibl);
            scene.getEnvironment().copyLightingFrom(cachedEnvironment);
        }
    }

    private static Map<String, Material> createMaterials(List<MaterialDefinition> definitions,
                                                         ResourceManager resources, Shader shader) {
        Map<String, Material> materials = new HashMap<>();

        if (definitions == null)
            return materials;

        for (MaterialDefinition definition : definitions) {
            if (definition.name == null || definition.name.isBlank())
                throw new IllegalArgumentException("Material name cannot be null or blank");

            if (materials.containsKey(definition.name))
                throw new IllegalArgumentException("Duplicate material name: " + definition.name);

            materials.put(definition.name, createMaterial(definition, resources, shader));
        }

        return materials;
    }

    private static Material createMaterial(MaterialDefinition definition, ResourceManager resources, Shader shader) {
        Material material = new Material(shader);

        if (definition.albedo != null)
            material.setAlbedo(resources.createTexture(definition.albedo));

        if (definition.normal != null)
            material.setNormalMap(resources.createDataTexture(definition.normal));

        if (definition.roughness != null)
            material.setRoughness(definition.roughness);

        if (definition.metallic != null)
            material.setMetallic(definition.metallic);

        if (definition.normalStrength != null)
            material.setNormalStrength(definition.normalStrength);

        return material;
    }

    private static void applyObjects(List<ObjectDefinition> objects, Scene scene, ResourceManager resources,
                                     Shader shader, Map<String, Material> materials) {
        if (objects == null)
            return;

        for (ObjectDefinition object : objects) {
            SceneObject sceneObject = createSceneObject(object, resources, shader, materials);

            applyTransform(object, sceneObject.getTransform());
            applyTags(object, sceneObject);
            applyLayerMask(object, sceneObject);
            applyComponents(object, sceneObject);
            applyChildren(object, sceneObject, resources, shader, materials);
            applyTrigger(object, sceneObject);

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

    private static void applyTags(ObjectDefinition object, SceneObject sceneObject) {
        if (object.tags == null)
            return;

        for (String tag : object.tags)
            sceneObject.addTag(tag);
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
                case "keyframeAnimation" -> {
                    if (component.keyframes == null || component.keyframes.isEmpty())
                        throw new IllegalArgumentException(
                            object.name + ".keyframeAnimation.keyframes must contain at least one keyframe"
                        );

                    List<TransformKeyframe> keyframes = component.keyframes.stream()
                        .map(SceneLoader::toTransformKeyframe)
                        .toList();

                    sceneObject.addComponent(
                        new KeyframeAnimationComponent(keyframes)
                            .setLoop(component.loop == null || component.loop)
                    );
                }
                default -> throw new IllegalArgumentException("Unsupported component type: " + component.type);
            }
        }
    }

    private static void applyLayerMask(ObjectDefinition object, SceneObject sceneObject) {
        if (object.layerMask != null && object.layers != null)
            throw new IllegalArgumentException(object.name + " cannot define both layerMask and layers");

        if (object.layerMask != null) {
            sceneObject.setLayerMask(object.layerMask);
            return;
        }

        if (object.layers != null) {
            sceneObject.setLayerMask(resolveLayerNames(object.name, object.layers));
        }
    }

    private static int resolveLayerNames(String objectName, List<String> layers) {
        if (layers.isEmpty()) throw new IllegalArgumentException(objectName + ".layers cannot be empty");

        int mask = 0;

        for (String layer : layers)
            mask |= resolveLayerName(objectName, layer);

        return mask;
    }

    private static int resolveLayerName(String objectName, String layer) {
        try {
            return RenderLayers.fromName(layer);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(objectName + " references invalid layer: " + layer, e);
        }
    }

    private static void applyChildren(ObjectDefinition object, SceneObject parent, ResourceManager resources,
                                      Shader shader, Map<String, Material> materials) {
        if (object.children == null)
            return;

        for (ObjectDefinition childDefinition : object.children) {
            SceneObject child = createSceneObject(childDefinition, resources, shader, materials);
            applyTransform(childDefinition, child.getTransform());
            applyTags(childDefinition, child);
            applyLayerMask(childDefinition, child);
            applyComponents(childDefinition, child);
            applyChildren(childDefinition, child, resources, shader, materials);
            applyTrigger(childDefinition, child);

            if (childDefinition.castsShadow != null)
                child.setCastsShadowRecursive(childDefinition.castsShadow);

            parent.addChild(child);
        }
    }

    private static void applyTrigger(ObjectDefinition object, SceneObject sceneObject) {
        if (object.trigger == null)
            return;

        float[] halfExtents = vec3(object.trigger.halfExtents, object.name + ".trigger.halfExtents");
        sceneObject.setTriggerVolume(new TriggerVolume(halfExtents[0], halfExtents[1], halfExtents[2]));
    }

    private static SceneObject loadModelObject(ObjectDefinition object, ResourceManager resources, Shader shader) {
        if (object.path == null || object.path.isBlank())
            throw new IllegalArgumentException("Model object path cannot be null or blank");

        Model model = resources.loadModel(object.path, new ModelLoadOptions().setShader(shader));

        SceneObject root = model.instantiate();

        if (object.name != null)
            root.setName(object.name);

        root.setSource(SceneObjectSource.model(object.path));

        return root;
    }

    private static SceneObject createPrimitiveObject(ObjectDefinition object, ResourceManager resources,
                                                     Shader shader, Map<String, Material> materials) {
        if (object.primitive == null || object.primitive.isBlank())
            throw new IllegalArgumentException("Primitive object must define primitive");

        Mesh mesh = switch (object.primitive) {
            case "plane" -> resources.getLitTexturedPlaneMesh();
            case "cube" -> resources.getLitTexturedCubeMesh();
            default -> throw new IllegalArgumentException("Unsupported primitive: " + object.primitive);
        };

        Material material = resolveMaterial(object, materials, shader);

        return new SceneObject(object.name, mesh, material, new Transform())
            .setSource(SceneObjectSource.primitive(object.primitive, object.material));
    }

    private static Material resolveMaterial(ObjectDefinition object, Map<String, Material> materials, Shader shader) {
        if (object.material == null || object.material.isBlank())
            return new Material(shader);

        Material material = materials.get(object.material);

        if (material == null)
            throw new IllegalArgumentException(
                "Object '" + object.name + "' references unknown material: " + object.material
            );

        return material;
    }

    private static SceneObject createSceneObject(ObjectDefinition object, ResourceManager resources,
                                                 Shader shader, Map<String, Material> materials) {
        return switch (object.type) {
            case "model" -> loadModelObject(object, resources, shader);
            case "primitive" -> createPrimitiveObject(object, resources, shader, materials);
            case "empty" -> new SceneObject(object.name, null, null, new Transform())
                .setSource(SceneObjectSource.empty());
            default -> throw new IllegalArgumentException("Unsupported scene object type: " + object.type);
        };
    }

    private static TransformKeyframe toTransformKeyframe(KeyframeDefinition keyframe) {
        return new TransformKeyframe(
            keyframe.time,
            optionalVec3(keyframe.position),
            optionalVec3(keyframe.rotationDegrees),
            optionalVec3(keyframe.scale)
        );
    }

    private static Vector3f optionalVec3(List<Float> values) {
        if (values == null)
            return null;

        float[] vector = vec3(values, "animation keyframe");
        return new Vector3f(vector[0], vector[1], vector[2]);
    }

    private static float[] vec3(List<Float> values, String fieldName) {
        if (values.size() != 3) throw new IllegalArgumentException(fieldName + " must contain exactly 3 values");

        return new float[] { values.get(0), values.get(1), values.get(2) };
    }

    private static String sceneName(SceneDefinition definition, Path scenePath) {
        return definition.name != null && !definition.name.isBlank()
            ? definition.name
            : scenePath.getFileName().toString();
    }

    private static int sceneVersion(SceneDefinition definition) {
        return definition.version != null ? definition.version : 1;
    }
}
