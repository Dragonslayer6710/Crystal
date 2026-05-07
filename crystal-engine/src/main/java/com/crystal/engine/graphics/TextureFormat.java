package com.crystal.engine.graphics;

import static org.lwjgl.opengl.GL21.*;

public enum TextureFormat {
    RGBA8(GL_RGBA8),
    SRGBA8(GL_SRGB8_ALPHA8);

    public final int glValue;

    TextureFormat(int glValue) { this.glValue = glValue; }
}
