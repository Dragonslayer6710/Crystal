package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderCommand;

import static org.lwjgl.opengl.GL46.*;

public class ClearCommand implements RenderCommand {

    private final float r, g, b, a;

    public ClearCommand(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public void execute() {
        glClearColor(r, g, b, a);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
}
