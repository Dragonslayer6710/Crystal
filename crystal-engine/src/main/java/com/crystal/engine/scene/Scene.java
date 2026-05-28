package com.crystal.engine.scene;

import com.crystal.engine.render.environment.Environment;
import com.crystal.engine.scene.camera.Camera;
import com.crystal.engine.scene.collision.BoxCollider;
import com.crystal.engine.scene.component.*;
import com.crystal.engine.scene.light.DirectionalLight;
import com.crystal.engine.scene.source.SceneEnvironmentSource;
import com.crystal.engine.scene.source.SceneMaterialSource;
import org.joml.Vector3f;

import java.util.*;

public class Scene {

    private final List<SceneObject> rootObjects = new ArrayList<>();
    private final Camera camera = new Camera(0, 0, 0);

    private final DirectionalLight directionalLight = new DirectionalLight();
    private final Environment environment = new Environment();
    private SceneEnvironmentSource environmentSource;

    private CameraComponent activeCamera;

    private final Map<String, SceneMaterialSource> materialSources = new LinkedHashMap<>();

    public void add(SceneObject object) {
        if (object == null) throw new IllegalArgumentException("SceneObject cannot be null");
        if (object.getParent() != null) throw new IllegalArgumentException("Only root objects can be added to Scene");

        if (rootObjects.contains(object))
            return;

        rootObjects.add(object);
    }

    public boolean remove(SceneObject object) {
        if (object == null)
            return false;

        boolean removed = rootObjects.remove(object);

        if (removed)
            object.getTransform().setParent(null);

        return removed;
    }

    public List<SceneObject> getRootObjects() {
        return rootObjects;
    }

    public void update(SceneUpdateContext context) {
        for (SceneObject object : rootObjects)
            object.update(context);
    }

    public Camera getCamera() {
        if (activeCamera != null && activeCamera.isEnabled())
            return activeCamera.getCamera();

        return camera;
    }

    public Optional<CameraComponent> getActiveCamera() {
        return Optional.ofNullable(activeCamera);
    }

    public void setActiveCamera(CameraComponent activeCamera) {
        this.activeCamera = activeCamera;
    }

    public void clearActiveCamera() {
        activeCamera = null;
    }

    public List<DirectionalLightComponent> getDirectionalLights() {
        return findComponents(DirectionalLightComponent.class)
            .stream()
            .filter(DirectionalLightComponent::isEnabled)
            .toList();
    }

    public DirectionalLight getDirectionalLight() {
        return getDirectionalLights()
            .stream()
            .findFirst()
            .map(DirectionalLightComponent::getLight)
            .orElse(directionalLight);
    }

    public Optional<DirectionalLightComponent> getActiveDirectionalLight() {
        return getDirectionalLights()
            .stream()
            .findFirst();
    }

    public List<PointLightComponent> getPointLights() {
        return findComponents(PointLightComponent.class)
            .stream()
            .filter(PointLightComponent::isEnabled)
            .toList();
    }

    public Environment getEnvironment() {
        return environment;
    }

    public SceneEnvironmentSource getEnvironmentSource() {
        return environmentSource;
    }

    public void setEnvironmentSource(SceneEnvironmentSource environmentSource) {
        this.environmentSource = environmentSource;
    }

    public void addMaterialSource(SceneMaterialSource source) {
        if (source == null) throw new IllegalArgumentException("Material source cannot be null");

        materialSources.put(source.getName(), source);
    }

    public List<SceneMaterialSource> getMaterialSources() {
        return List.copyOf(materialSources.values());
    }

    public void clearMaterialSources() {
        materialSources.clear();
    }

    public Optional<SceneObject> findByName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be null or blank");

        for (SceneObject object : rootObjects) {
            Optional<SceneObject> match = findByName(object, name);

            if (match.isPresent())
                return match;
        }

        return Optional.empty();
    }

    public <T extends SceneComponent> Optional<T> findFirstComponent(Class<T> type) {
        if (type == null) throw new IllegalArgumentException("Component type cannot be null");

        for (SceneObject object : rootObjects) {
            Optional<T> match = findFirstComponent(object, type);

            if (match.isPresent())
                return match;
        }

        return Optional.empty();
    }

    private <T extends SceneComponent> Optional<T> findFirstComponent(SceneObject object, Class<T> type) {
        T component = object.getComponent(type);

        if (component != null)
            return Optional.of(component);

        for (SceneObject child : object.getChildren()) {
            Optional<T> match = findFirstComponent(child, type);

            if (match.isPresent())
                return match;
        }

        return Optional.empty();
    }

    public <T extends SceneComponent> List<T> findComponents(Class<T> type) {
        if (type == null) throw new IllegalArgumentException("Component type cannot be null");

        List<T> components = new ArrayList<>();

        for (SceneObject object : rootObjects)
            findComponents(object, type, components);

        return components;
    }

    private <T extends SceneComponent> void findComponents(SceneObject object, Class<T> type, List<T> components) {
        T component = object.getComponent(type);

        if (component != null)
            components.add(component);

        for (SceneObject child : object.getChildren())
            findComponents(child, type, components);
    }

    private Optional<SceneObject> findByName(SceneObject object, String name) {
        if (name.equals(object.getName()))
            return Optional.of(object);

        for (SceneObject child : object.getChildren()) {
            Optional<SceneObject> match = findByName(child, name);

            if (match.isPresent())
                return match;
        }

        return Optional.empty();
    }

    public List<SceneObject> findByTag(String tag) {
        if (tag == null || tag.isBlank()) throw new IllegalArgumentException("Tag cannot be null or blank");

        List<SceneObject> matches = new ArrayList<>();

        for (SceneObject object : rootObjects)
            findByTag(object, tag, matches);

        return matches;
    }

    private void findByTag(SceneObject object, String tag, List<SceneObject> matches) {
        if (object.hasTag(tag))
            matches.add(object);

        for (SceneObject child : object.getChildren())
            findByTag(child, tag, matches);
    }

    public List<SceneObject> findTriggersContaining(Vector3f point) {
        if (point == null) throw new IllegalArgumentException("Point cannot be null");

        List<SceneObject> matches = new ArrayList<>();

        for (SceneObject object : rootObjects)
            findTriggersContaining(object, point, matches);

        return matches;
    }

    private void findTriggersContaining(SceneObject object, Vector3f point, List<SceneObject> matches) {
        if (!object.isActive())
            return;

        TriggerVolumeComponent trigger = object.getComponent(TriggerVolumeComponent.class);

        if (trigger != null && trigger.contains(point))
            matches.add(object);

        for (SceneObject child : object.getChildren())
            findTriggersContaining(child, point, matches);
    }

    public List<SceneObject> findCollidersContaining(Vector3f point) {
        if (point == null) throw new IllegalArgumentException("Point cannot be null");

        List<SceneObject> matches = new ArrayList<>();

        for (SceneObject object : rootObjects)
            findCollidersContaining(object, point, matches);

        return matches;
    }

    private void findCollidersContaining(SceneObject object, Vector3f point, List<SceneObject> matches) {
        if (!object.isActive())
            return;

        BoxColliderComponent collider = object.getComponent(BoxColliderComponent.class);

        if (collider != null && collider.contains(point))
            matches.add(object);

        for (SceneObject child : object.getChildren())
            findCollidersContaining(child, point, matches);
    }

    public List<SceneObject> findCollidersIntersecting(BoxCollider collider, Transform transform) {
        if (collider == null) throw new IllegalArgumentException("Collider cannot be null");
        if (transform == null) throw new IllegalArgumentException("Transform cannot be null");

        List<SceneObject> matches = new ArrayList<>();

        for (SceneObject object : rootObjects)
            findCollidersIntersecting(object, collider, transform, matches);

        return matches;
    }

    private void findCollidersIntersecting(
        SceneObject object,
        BoxCollider collider,
        Transform transform,
        List<SceneObject> matches
    ) {
        if (!object.isActive())
            return;

        BoxColliderComponent objectCollider = object.getComponent(BoxColliderComponent.class);

        if (objectCollider != null && objectCollider.intersects(collider, transform))
            matches.add(object);

        for (SceneObject child : object.getChildren())
            findCollidersIntersecting(child, collider, transform, matches);
    }

    public void replaceWith(Scene source) {
        if (source == null) throw new IllegalArgumentException("Source scene cannot be null");

        clear();

        var srcTransform = source.getCamera().getTransform();
        camera.getTransform().setPosition(
            srcTransform.getPosition().x,
            srcTransform.getPosition().y,
            srcTransform.getPosition().z
        );

        camera.getTransform().setRotation(srcTransform.getRotationQuat());

        var srcDirLight = source.getDirectionalLight();
        directionalLight
            .setIntensity(srcDirLight.getIntensity())
            .setShadowStrength(srcDirLight.getShadowStrength());

        var srcEnv = source.getEnvironment();
        environment
            .setAmbientColor(
                srcEnv.getAmbientColor().x,
                srcEnv.getAmbientColor().y,
                srcEnv.getAmbientColor().z
            )
            .setAmbientIntensity(srcEnv.getAmbientIntensity())
            .setIblDiffuseIntensity(srcEnv.getIblDiffuseIntensity())
            .setIblSpecularIntensity(srcEnv.getIblSpecularIntensity())
            .copyLightingFrom(srcEnv);

        setEnvironmentSource(source.getEnvironmentSource());

        clearMaterialSources();

        for (SceneMaterialSource materialSource : source.getMaterialSources())
            addMaterialSource(materialSource);

        for (SceneObject object : source.getRootObjects())
            add(object);
    }

    public void clear() {
        for (SceneObject object : rootObjects)
            object.getTransform().setParent(null);

        rootObjects.clear();
    }

}
