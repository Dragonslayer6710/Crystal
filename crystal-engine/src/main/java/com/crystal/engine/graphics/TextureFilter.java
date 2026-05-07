package com.crystal.engine.graphics;

import static org.lwjgl.opengl.GL11.*;

public enum TextureFilter {
    NEAREST(GL_NEAREST),
    LINEAR(GL_LINEAR),
    NEAREST_MIPMAP_NEAREST(GL_NEAREST_MIPMAP_NEAREST),
    LINEAR_MIPMAP_NEAREST(GL_LINEAR_MIPMAP_NEAREST),
    NEAREST_MIPMAP_LINEAR(GL_NEAREST_MIPMAP_LINEAR),
    LINEAR_MIPMAP_LINEAR(GL_LINEAR_MIPMAP_LINEAR);


    public final int glValue;

    TextureFilter(int glValue) { this.glValue = glValue; }
}
