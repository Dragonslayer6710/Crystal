package com.crystal.engine.graphics;


import static org.lwjgl.opengl.GL13.*;

public enum TextureTarget {
    TEXTURE_2D(GL_TEXTURE_2D),
    CUBE_MAP(GL_TEXTURE_CUBE_MAP);

    public final int glValue;

    TextureTarget(int glValue) { this.glValue = glValue; }
}
