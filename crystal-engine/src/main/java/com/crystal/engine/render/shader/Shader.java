package com.crystal.engine.render.shader;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.render.GLObjectLabel;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL46.*;

public class Shader implements Disposable {

    private final String vertexSourcePath;
    private final String fragmentSourcePath;

    private final int id;
    private final Map<String, Integer> uniformMap = new HashMap<>();

    private boolean disposed;

    public Shader(String vertexSrc, String fragmentSrc, String vertexSourcePath, String fragmentSourcePath) {
        this.vertexSourcePath = vertexSourcePath;
        this.fragmentSourcePath = fragmentSourcePath;

        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, vertexSrc);
        glCompileShader(vs);
        checkCompile(vs, "VERTEX", vertexSourcePath);

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, fragmentSrc);
        glCompileShader(fs);
        checkCompile(fs, "FRAGMENT", fragmentSourcePath);

        id = glCreateProgram();
        glAttachShader(id, vs);
        glAttachShader(id, fs);

        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            throw new ShaderException(
                    "\nShader program link failed" +
                            "\nVertex: " + vertexSourcePath +
                            "\nFragment: " + fragmentSourcePath +
                            "\n\n--- OpenGL Log ---\n" +
                            glGetProgramInfoLog(id)
            );
        }

        glDeleteShader(vs);
        glDeleteShader(fs);
    }

    public Shader(String vertexSrc, String fragmentSrc) {
        this(vertexSrc, fragmentSrc, null, null);
    }

    private void checkCompile(int shaderId, String stage, String sourcePath) {
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shaderId);

            throw new ShaderException(
                    "\nShader compilation failed" +
                            "\nStage: " + stage +
                            "\nFile: " + sourcePath +
                            "\n\n--- OpenGL Log ---\n" +
                            log
            );
        }
    }

    private int getUniformLocation(String uniformName) {
        if (!uniformMap.containsKey(uniformName)) {
            uniformMap.put(uniformName, glGetUniformLocation(id, uniformName));
        }
        return uniformMap.get(uniformName);
    }

    public void setInt(String name, int value) {
        glUniform1i(getUniformLocation(name), value);
    }

    public void setFloat(String name, float value) {
        glUniform1f(getUniformLocation(name), value);
    }

    public void setVec3(String name, float x, float y, float z) {
        glUniform3f(getUniformLocation(name), x, y, z);
    }

    public void setMat4(String name, Matrix4f mat) {
        try (var stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(
                    getUniformLocation(name),
                    false,
                    mat.get(stack.mallocFloat(16))
            );
        }
    }

    public void setDebugLabel(String label) {
        GLObjectLabel.labelProgram(id, label);
    }

    public int getId() {
        return id;
    }

    public String getVertexSourcePath() {
        return vertexSourcePath;
    }

    public String getFragmentSourcePath() {
        return fragmentSourcePath;
    }

    public void bind() {
        glUseProgram(id);
    }

    @Override
    public void dispose() {
        if (disposed) return;

        glDeleteProgram(id);
        disposed = true;
    }

}
