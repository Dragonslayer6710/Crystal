package com.crystal.engine.render.mesh;

import static org.lwjgl.opengl.GL11.*;

public enum PrimitiveType {

    TRIANGLES(GL_TRIANGLES),
    LINES(GL_LINES),
    POINTS(GL_POINTS);

    public final int glValue;

    PrimitiveType(int glValue) { this.glValue = glValue; }
}
