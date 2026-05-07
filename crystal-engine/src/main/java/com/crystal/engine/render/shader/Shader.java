package com.crystal.engine.render.shader;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.render.GLObjectLabel;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL46.*;

public class Shader implements Disposable {

    public static final class Uniforms {
        private Uniforms() {}

        public static final String MODEL = "model";
        public static final String VIEW = "view";
        public static final String PROJECTION = "projection";

        public static final String ALBEDO_TEXTURE = "albedoTexture";
        public static final String NORMAL_MAP = "normalMap";
        public static final String MATERIAL_TINT = "materialTint";

        public static final String AMBIENT_COLOR = "ambientColor";
        public static final String AMBIENT_INTENSITY = "ambientIntensity";

        public static final String SUN_DIRECTION = "sun.direction";
        public static final String SUN_COLOR = "sun.color";
        public static final String SUN_INTENSITY = "sun.intensity";
    }

    private final int id;
    private final Map<String, Integer> uniformMap = new HashMap<>();


    public Shader(String vertexSrc, String fragmentSrc) {
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, vertexSrc);
        glCompileShader(vs);
        checkCompile(vs);

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, fragmentSrc);
        glCompileShader(fs);
        checkCompile(fs);

        id = glCreateProgram();
        glAttachShader(id, vs);
        glAttachShader(id, fs);

        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException(glGetProgramInfoLog(id));
        }

        glDeleteShader(vs);
        glDeleteShader(fs);
    }

    private void checkCompile(int shaderId) {
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException(glGetShaderInfoLog(shaderId));
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

    public int getId() {
        return id;
    }

    public void setDebugLabel(String label) {
        GLObjectLabel.labelProgram(id, label);
    }

    public void bind() {
        glUseProgram(id);
    }

    @Override
    public void dispose() {
        glDeleteProgram(id);
    }

}
