package com.crystal.engine.render;

import com.crystal.engine.render.commands.DrawSceneObjectCommand;
import com.crystal.engine.render.scene.SceneObject;
import com.crystal.engine.render.scene.Scene;

import static org.lwjgl.opengl.GL46.*;

public class Renderer {

    private final RenderQueue queue = new RenderQueue();

    public void init(int width, int height) {
        resizeViewport(width, height);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
    }

    // Called at start of frame
    public void beginFrame() {
        queue.clear();
        glClearColor(0.1f, 0.1f, 0.15f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    // Called at end of frame
    public void renderFrame() {
        queue.execute();
    }

    public void render(Scene scene, float aspectRatio) {
        beginFrame();
        for (SceneObject r : scene.getRenderables()) {
            // later: use r.getTransform()
            queue.submit(new DrawSceneObjectCommand(r, scene, aspectRatio));
        }
        renderFrame();
    }

    public void resizeViewport(int width, int height) {
        glViewport(0, 0, width, height);
    }
}
