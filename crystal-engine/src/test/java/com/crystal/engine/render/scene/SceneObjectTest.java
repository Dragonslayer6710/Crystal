package com.crystal.engine.render.scene;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneObjectTest {

    @Test
    void addChildSetsParentAndTransformParent() {
        SceneObject parent = object("parent");
        SceneObject child = object("child");

        parent.addChild(child);

        assertSame(parent, child.getParent());
        assertSame(parent.getTransform(), child.getTransform().getParent());
        assertTrue(parent.getChildren().contains(child));
    }

    @Test
    void removeChildClearsParentAndTransformParent() {
        SceneObject parent = object("parent");
        SceneObject child = object("child");
        parent.addChild(child);

        parent.removeChild(child);

        assertNull(child.getParent());
        assertNull(child.getTransform().getParent());
        assertTrue(parent.getChildren().isEmpty());
    }

    @Test
    void reparentingRemovesChildFromOldParent() {
        SceneObject oldParent = object("oldParent");
        SceneObject newParent = object("newParent");
        SceneObject child = object("child");

        oldParent.addChild(child);
        newParent.addChild(child);

        assertTrue(oldParent.getChildren().isEmpty());
        assertEquals(1, newParent.getChildren().size());
        assertSame(newParent, child.getParent());
        assertSame(newParent.getTransform(), child.getTransform().getParent());
    }

    @Test
    void childrenListCannotBeMutatedDirectly() {
        SceneObject parent = object("parent");
        SceneObject child = object("child");

        assertThrows(
                UnsupportedOperationException.class,
                () -> parent.getChildren().add(child)
        );
    }

    @Test
    void addChildRejectsSelfParenting() {
        SceneObject object = object("object");

        assertThrows(IllegalArgumentException.class, () -> object.addChild(object));
    }

    @Test
    void addChildRejectsCircularHierarchy() {
        SceneObject root = object("root");
        SceneObject child = object("child");
        SceneObject grandchild = object("grandchild");

        root.addChild(child);
        child.addChild(grandchild);

        assertThrows(IllegalArgumentException.class, () -> grandchild.addChild(root));
    }

    @Test
    void constructorRejectsNullTransform() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SceneObject("invalid", null, null, null)
        );
    }

    @Test
    void castsShadowDefaultsToTrue() {
        SceneObject object = object("object");

        assertTrue(object.castsShadow());
    }

    @Test
    void setCastsShadowUpdatesShadowCasting() {
        SceneObject object = object("object");

        object.setCastsShadow(false);

        assertFalse(object.castsShadow());
    }

    @Test
    void setCastsShadowRecursiveUpdatesChildren() {
        SceneObject parent = object("parent");
        SceneObject child = object("child");
        SceneObject grandchild = object("grandchild");

        parent.addChild(child);
        child.addChild(grandchild);

        parent.setCastsShadowRecursive(false);

        assertFalse(parent.castsShadow());
        assertFalse(child.castsShadow());
        assertFalse(grandchild.castsShadow());
    }

    private static SceneObject object(String name) {
        return new SceneObject(name, null, null, new Transform());
    }
}
