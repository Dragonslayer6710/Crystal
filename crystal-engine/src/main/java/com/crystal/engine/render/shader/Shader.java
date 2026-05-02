package com.crystal.engine.render.shader;

import static org.lwjgl.opengl.GL46.*;

public class Shader {

    private final int programId;

    public Shader(String vertexSrc, String fragmentSrc) {
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, vertexSrc);
        glCompileShader(vs);
        checkCompile(vs);

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, fragmentSrc);
        glCompileShader(fs);
        checkCompile(fs);

        programId = glCreateProgram();
        glAttachShader(programId, vs);
        glAttachShader(programId, fs);

        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException(glGetProgramInfoLog(programId));
        }

        glDeleteShader(vs);
        glDeleteShader(fs);
    }

    private void checkCompile(int shaderId) {
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException(glGetShaderInfoLog(shaderId));
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void delete() {
        glDeleteProgram(programId);
    }
}
