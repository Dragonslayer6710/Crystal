package com.crystal.engine.assets.model;

import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Transform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void addRootObjectStoresObject() {
        Model model = new Model();
        SceneObject object = new SceneObject("root", null, null, new Transform());

        model.addRootObject(object);

        assertEquals(1, model.getRootObjects().size());
        assertSame(object, model.getRootObjects().getFirst());
    }

    @Test
    void addRootObjectRejectsNullObject() {
        Model model = new Model();

        assertThrows(IllegalArgumentException.class, () -> model.addRootObject(null));
    }
}
