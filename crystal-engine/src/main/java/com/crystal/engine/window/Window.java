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

    private int width;
    private int height;
    private String title;

    private float aspectRatio;

    private WindowEventListener windowEventListener;
    private InputListener inputListener;

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;

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

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        handle = glfwCreateWindow(width, height, title, NULL, NULL);

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

                inputListener.onKey(key, action == GLFW_PRESS);
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

                inputListener.onMouse(btn, action == GLFW_PRESS);
            }));
        }

        if (handle == NULL) {
            throw new RuntimeException("Failed to create window");
        }

        glfwMakeContextCurrent(handle);

        glfwSwapInterval(0); // vsync
        glfwShowWindow(handle);
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
