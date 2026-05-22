package com.crystal.engine.render.scene;

import com.crystal.engine.input.Input;
import com.crystal.engine.window.Window;
import com.crystal.engine.window.WindowConfig;
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
    void addComponentAttachesComponentToObject() {
        SceneObject object = object("object");
        TestComponent component = new TestComponent();

        object.addComponent(component);

        assertSame(object, component.getOwner());
        assertSame(object, component.attachedOwner);
        assertTrue(object.getComponents().contains(component));
    }

    @Test
    void addComponentRejectsNullComponent() {
        SceneObject object = object("object");

        assertThrows(IllegalArgumentException.class, () -> object.addComponent(null));
    }

    @Test
    void addComponentRejectsAttachedComponent() {
        SceneObject first = object("first");
        SceneObject second = object("second");
        TestComponent component = new TestComponent();

        first.addComponent(component);

        assertThrows(IllegalStateException.class, () -> second.addComponent(component));
    }

    @Test
    void removeComponentDetachesComponentFromObject() {
        SceneObject object = object("object");
        TestComponent component = new TestComponent();
        object.addComponent(component);

        boolean removed = object.removeComponent(component);

        assertTrue(removed);
        assertNull(component.getOwner());
        assertSame(object, component.detachedOwner);
        assertTrue(object.getComponents().isEmpty());
    }

    @Test
    void removeComponentReturnsFalseForMissingComponent() {
        SceneObject object = object("object");

        assertFalse(object.removeComponent(new TestComponent()));
        assertFalse(object.removeComponent(null));
    }

    @Test
    void componentsListCannotBeMutatedDirectly() {
        SceneObject object = object("object");

        assertThrows(
                UnsupportedOperationException.class,
                () -> object.getComponents().add(new TestComponent())
        );
    }

    @Test
    void getComponentReturnsFirstMatchingComponent() {
        SceneObject object = object("object");
        TestComponent component = new TestComponent();

        object.addComponent(component);

        assertSame(component, object.getComponent(TestComponent.class));
        assertSame(component, object.getComponent(SceneComponent.class));
        assertNull(object.getComponent(OtherComponent.class));
    }

    @Test
    void getComponentRejectsNullType() {
        SceneObject object = object("object");

        assertThrows(IllegalArgumentException.class, () -> object.getComponent(null));
    }

    @Test
    void updateUpdatesEnabledComponentsAndChildren() {
        SceneObject parent = object("parent");
        SceneObject child = object("child");
        TestComponent parentComponent = new TestComponent();
        TestComponent childComponent = new TestComponent();

        parent.addComponent(parentComponent);
        child.addComponent(childComponent);
        parent.addChild(child);

        parent.update(context(0.5));

        assertEquals(1, parentComponent.updateCount);
        assertEquals(0.5, parentComponent.lastDeltaTime);
        assertEquals(1, childComponent.updateCount);
        assertEquals(0.5, childComponent.lastDeltaTime);
    }

    @Test
    void updateStartsComponentOnceBeforeFirstUpdate() {
        SceneObject object = object("object");
        TestComponent component = new TestComponent();
        object.addComponent(component);

        object.update(context(0.5));
        object.update(context(0.25));

        assertEquals(1, component.startCount);
        assertEquals(2, component.updateCount);
        assertEquals(0.5, component.startDeltaTime);
    }

    @Test
    void updateSkipsDisabledComponents() {
        SceneObject object = object("object");
        TestComponent component = new TestComponent();
        component.setEnabled(false);
        object.addComponent(component);

        object.update(context(0.5));

        assertEquals(0, component.updateCount);
        assertEquals(0, component.startCount);
    }

    @Test
    void disabledComponentStartsWhenReEnabledAndUpdated() {
        SceneObject object = object("object");
        TestComponent component = new TestComponent();
        component.setEnabled(false);
        object.addComponent(component);

        object.update(context(0.5));
        component.setEnabled(true);
        object.update(context(0.25));

        assertEquals(1, component.startCount);
        assertEquals(1, component.updateCount);
        assertEquals(0.25, component.startDeltaTime);
    }

    @Test
    void setEnabledCallsHooksOnlyWhenStateChanges() {
        TestComponent component = new TestComponent();

        component.setEnabled(false);
        component.setEnabled(false);
        component.setEnabled(true);
        component.setEnabled(true);

        assertEquals(1, component.disableCount);
        assertEquals(1, component.enableCount);
    }

    @Test
    void updateSkipsInactiveObjectHierarchy() {
        SceneObject parent = object("parent");
        SceneObject child = object("child");
        TestComponent parentComponent = new TestComponent();
        TestComponent childComponent = new TestComponent();

        parent.addComponent(parentComponent);
        child.addComponent(childComponent);
        parent.addChild(child);
        parent.setActive(false);

        parent.update(context(0.5));

        assertEquals(0, parentComponent.updateCount);
        assertEquals(0, childComponent.updateCount);
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

    private static SceneUpdateContext context(double deltaTime) {
        return new SceneUpdateContext(deltaTime, new Input(), new Window(new WindowConfig()));
    }

    private static class TestComponent extends SceneComponent {
        private SceneObject attachedOwner;
        private SceneObject detachedOwner;
        private int updateCount;
        private int startCount;
        private int enableCount;
        private int disableCount;
        private double lastDeltaTime;
        private double startDeltaTime;

        @Override
        protected void onAttach(SceneObject owner) {
            attachedOwner = owner;
        }

        @Override
        protected void onDetach(SceneObject owner) {
            detachedOwner = owner;
        }

        @Override
        protected void onStart(SceneUpdateContext context) {
            startCount++;
            startDeltaTime = context.getDeltaTime();
        }

        @Override
        protected void onEnable() {
            enableCount++;
        }

        @Override
        protected void onDisable() {
            disableCount++;
        }

        @Override
        public void update(SceneUpdateContext context) {
            updateCount++;
            lastDeltaTime = context.getDeltaTime();
        }
    }

    private static final class OtherComponent extends SceneComponent {
    }
}
