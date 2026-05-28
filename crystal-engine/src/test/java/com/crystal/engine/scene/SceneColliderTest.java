package com.crystal.engine.scene;

import com.crystal.engine.scene.collision.BoxCollider;
import com.crystal.engine.scene.component.BoxColliderComponent;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SceneColliderTest {

    @Test
    void findCollidersContainingTraversesActiveHierarchy() {
        Scene scene = new Scene();

        SceneObject parent = object("parent", 0.0f, 0.0f, 0.0f)
            .addComponent(new BoxColliderComponent(1.0f, 1.0f, 1.0f));
        SceneObject child = object("child", 2.0f, 0.0f, 0.0f)
            .addComponent(new BoxColliderComponent(1.0f, 1.0f, 1.0f));
        SceneObject inactive = object("inactive", 0.0f, 0.0f, 0.0f)
            .addComponent(new BoxColliderComponent(1.0f, 1.0f, 1.0f))
            .setActive(false);

        parent.addChild(child);
        scene.add(parent);
        scene.add(inactive);

        List<SceneObject> matches = scene.findCollidersContaining(new Vector3f(2.0f, 0.0f, 0.0f));

        assertEquals(List.of(child), matches);
    }

    @Test
    void findCollidersIntersectingReturnsOverlappingActiveColliders() {
        Scene scene = new Scene();

        SceneObject wall = object("wall", 2.0f, 0.0f, 0.0f)
            .addComponent(new BoxColliderComponent(1.0f, 1.0f, 1.0f));
        SceneObject inactive = object("inactive", 2.0f, 0.0f, 0.0f)
            .addComponent(new BoxColliderComponent(1.0f, 1.0f, 1.0f))
            .setActive(false);

        scene.add(wall);
        scene.add(inactive);

        BoxCollider mover = new BoxCollider(0.5f, 0.5f, 0.5f);
        Transform moverTransform = new Transform().setPosition(0.5f, 0.0f, 0.0f);

        assertEquals(List.of(wall), scene.findCollidersIntersecting(mover, moverTransform));
    }

    @Test
    void colliderQueriesRejectInvalidArguments() {
        Scene scene = new Scene();

        assertThrows(IllegalArgumentException.class, () -> scene.findCollidersContaining(null));
        assertThrows(IllegalArgumentException.class, () -> scene.findCollidersIntersecting(null, new Transform()));
        assertThrows(IllegalArgumentException.class, () -> scene.findCollidersIntersecting(new BoxCollider(), null));
    }

    private static SceneObject object(String name, float x, float y, float z) {
        return new SceneObject(name, null, null, new Transform().setPosition(x, y, z));
    }
}
