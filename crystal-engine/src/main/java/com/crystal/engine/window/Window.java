package com.crystal.engine.window;

import com.crystal.engine.input.InputListener;
import com.crystal.engine.input.Key;
import com.crystal.engine.input.MouseButton;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private long handle;

    private final WindowConfig config;
    private int width;
    private int height;
    private String title;

    private float aspectRatio;

    private WindowEventListener windowEventListener;
    private InputListener inputListener;

    public Window(WindowConfig config) {
        if (config == null) throw new IllegalArgumentException("WindowConfig cannot be null");

        this.config = config;
        this.width = config.getWidth();
        this.height = config.getHeight();
        this.title = config.getTitle();

        this.aspectRatio = (float) width / (float) height;
    }

    public void setWindowEventListener(WindowEventListener eventListener) {
        this.windowEventListener = eventListener;
    }

    public void setInputListener(InputListener inputListener) {
        this.inputListener = inputListener;
    }

    public void create() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwWindowHint(GLFW_VISIBLE, config.isVisible() ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, config.isResizable() ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, config.isDebugContext() ? GLFW_TRUE : GLFW_FALSE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) throw new RuntimeException("Failed to create window");

        // Set Callbacks
        {
            glfwSetFramebufferSizeCallback(handle, (window, newWidth, newHeight) -> {
                this.width = newWidth;
                this.height = newHeight;

                if (newHeight != 0)
                    this.aspectRatio = (float) newWidth / (float) newHeight;

                if (windowEventListener != null)
                    windowEventListener.onFrameBufferResize(newWidth, newHeight);
            });

            glfwSetKeyCallback(handle, (window, keyCode, scancode, action, mods) -> {
                if (inputListener == null)
                    return;

                Key key = Key.fromCode(keyCode);

                if (key == null)
                    return;

                if (action == GLFW_PRESS) {
                    inputListener.onKey(key, true);
                } else if (action == GLFW_RELEASE) {
                    inputListener.onKey(key, false);
                }
            });

            glfwSetCursorPosCallback(handle, (window, x, y) -> {
                if (inputListener != null) {
                    inputListener.onMouseMove(x, y);
                }
            });

            glfwSetMouseButtonCallback(handle, ((window, button, action, mods) -> {
                if (inputListener ==  null)
                    return;

                MouseButton btn = MouseButton.fromCode(button);

                if (btn == null)
                    return;

                if (action == GLFW_PRESS) {
                    inputListener.onMouse(btn, true);
                } else if (action == GLFW_RELEASE) {
                    inputListener.onMouse(btn, false);
                }
            }));
        }

        glfwMakeContextCurrent(handle);

        glfwSwapInterval(config.isVSync() ? 1 : 0);

        if (config.isVisible()) glfwShowWindow(handle);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void setCursorCaptured(boolean captured) {
        if (captured) {
            glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        } else {
            glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    public long getHandle() {
        return handle;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void destroy() {
        Callbacks.glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
    }
}
