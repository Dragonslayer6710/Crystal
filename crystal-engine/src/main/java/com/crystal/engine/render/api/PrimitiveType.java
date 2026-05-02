package com.crystal.engine.render.api;

import static org.lwjgl.opengl.GL46.*;

public enum PrimitiveType {

    TRIANGLES(GL_TRIANGLES),
    LINES(GL_LINES),
    POINTS(GL_POINTS);

    public final int glValue;

    PrimitiveType(int glValue) {
        this.glValue = glValue;
    }
}
