package com.crystal.engine.graphics;

import static org.lwjgl.opengl.GL11.*;

public enum TextureFilter {
    NEAREST(GL_NEAREST),
    LINEAR(GL_LINEAR);

    public final int glValue;

    TextureFilter(int glValue) { this.glValue = glValue; }
}
