package com.crystal.engine.render.texture;


import static org.lwjgl.opengl.GL13.*;

public enum TextureWrap {
    REPEAT(GL_REPEAT),
    CLAMP_TO_EDGE(GL_CLAMP_TO_EDGE),
    CLAMP_TO_BORDER(GL_CLAMP_TO_BORDER);

    public final int glValue;

    TextureWrap(int glValue) { this.glValue = glValue; }
}
