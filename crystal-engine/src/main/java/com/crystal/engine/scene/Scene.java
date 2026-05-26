package com.crystal.engine.scene;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.render.environment.Environment;
import com.crystal.engine.render.opengl.UniformBuffer;
import com.crystal.engine.scene.camera.Camera;
import com.crystal.engine.scene.component.CameraComponent;
import com.crystal.engine.scene.light.DirectionalLight;
import com.crystal.engine.render.uniform.SceneUniformData;
import com.crystal.engine.scene.source.SceneEnvironmentSource;
import com.crystal.engine.scene.source.SceneMaterialSource;
import org.joml.Vector3f;

import java.util.*;

public class Scene implements Disposable {

    private final List<SceneObject> rootObjects = new ArrayList<>();
    private final Camera camera = new Camera(0, 0, 0);

    private CameraComponent activeCamera;

    private final DirectionalLight directionalLight = new DirectionalLight();
    private final Environment environment = new Environment();
    private SceneEnvironmentSource environmentSource;

    private final Map<String, SceneMaterialSource> materialSources = new LinkedHashMap<>();

    private final UniformBuffer sceneUBO = new UniformBuffer(
            SceneUniformData.BINDING_POINT,
            SceneUniformData.BYTE_SIZE
    );

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

    public DirectionalLight getDirectionalLight() {
        return directionalLight;
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

    public UniformBuffer getSceneUBO() {
        return sceneUBO;
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
        if (object.hasTriggerVolume() && object.getTriggerVolume().contains(point, object.getTransform()))
            matches.add(object);

        for (SceneObject child : object.getChildren())
            findTriggersContaining(child, point, matches);
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

    @Override
    public void dispose() {
        sceneUBO.dispose();
    }
}
