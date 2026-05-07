package com.crystal.engine.graphics;


import static org.lwjgl.opengl.GL12.*;

public enum TextureWrap {
    REPEAT(GL_REPEAT),
    CLAMP_TO_EDGE(GL_CLAMP_TO_EDGE);

    public final int glValue;

    TextureWrap(int glValue) { this.glValue = glValue; }
}
