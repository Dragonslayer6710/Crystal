package com.crystal.engine.graphics;

import static org.lwjgl.opengl.GL15.*;

public enum BufferUsage {
    STATIC(GL_STATIC_DRAW),
    DYNAMIC(GL_DYNAMIC_DRAW),
    STREAM(GL_STREAM_DRAW);

    public final int glValue;

    BufferUsage(int glValue) { this.glValue = glValue; }
}
