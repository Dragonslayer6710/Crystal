package com.crystal.engine.render;

import com.crystal.engine.core.Disposable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.opengl.GL43.*;

public final class GLDebug {

    private static final Logger logger = LoggerFactory.getLogger(GLDebug.class);

    private static GLDebugMessageCallback callback;

    private GLDebug() {}

    public static void init() {
        if (!GL.getCapabilities().GL_KHR_debug) {
            logger.warn("OpenGL debug output is not supported");
            return;
        }

        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);

        callback = GLDebugMessageCallback.create((source, type, id, severity,
                                                  length, message, userParam) -> {
            String msg = GLDebugMessageCallback.getMessage(length, message);

            switch (severity) {
                case GL_DEBUG_SEVERITY_HIGH -> logger.error("[OpenGL] {}", msg);
                case GL_DEBUG_SEVERITY_MEDIUM -> logger.warn("[OpenGL] {}", msg);
                case GL_DEBUG_SEVERITY_LOW -> logger.info("[OpenGL] {}", msg);
                case GL_DEBUG_SEVERITY_NOTIFICATION -> logger.debug("[OpenGL] {}", msg);
                default -> logger.warn("[OpenGL] Unknown severity: {}", msg);
            }
        });

        glDebugMessageCallback(callback, 0);

        logger.info("OpenGL debug callback initialised");
    }

    public static void dispose() {
        if (callback != null) {
            callback.free();
            callback = null;
        }
    }
}
