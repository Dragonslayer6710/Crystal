package com.crystal.engine.render.scene;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private final List<Renderable> renderables = new ArrayList<>();
    private final Camera camera = new Camera(0, 0, 0);

    public void add(Renderable renderable) {
        renderables.add(renderable);
    }

    public List<Renderable> getRenderables() {
        return renderables;
    }

    public Camera getCamera() {
        return camera;
    }
}
