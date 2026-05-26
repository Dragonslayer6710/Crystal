package com.crystal.engine.render.opengl.debug;

import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL43.*;

public final class GLObjectLabel {
    private GLObjectLabel() {}

    public static void labelTexture(int id, String label) { label(GL_TEXTURE, id, label); }
    public static void labelProgram(int id, String label) { label(GL_PROGRAM, id, label); }
    public static void labelFramebuffer(int id, String label) { label(GL_FRAMEBUFFER, id, label); }
    public static void labelRenderbuffer(int id, String label) { label(GL_RENDERBUFFER, id, label); }

    private static void label(int identifier, int id, String label) {
        if (id == 0 || label == null || label.isBlank()) return;

        if (!GL.getCapabilities().GL_KHR_debug) return;

        glObjectLabel(identifier, id, label);
    }
}
