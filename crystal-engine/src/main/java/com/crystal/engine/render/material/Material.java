package com.crystal.engine.render.material;

import com.crystal.engine.render.texture.Texture;
import com.crystal.engine.render.gl.UniformBuffer;
import com.crystal.engine.render.shader.ShaderProgram;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL46.*;

public class Material {

    private final ShaderProgram shaderProgram;

    private Texture albedo;
    private Texture normalMap;

    private UniformBuffer materialUBO;

    private final Vector3f tint = new Vector3f(1.0f);

    private final RenderState renderState = new RenderState();

    private static int nextId = 1;
    private final int id = nextId++;

    public Material(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    // ---------- GETTERS ----------

    public Vector3f getTint() {
        return tint;
    }

    public RenderState getRenderState() {
        return renderState;
    }

    // ---------- SETTERS (explicit, no collections) ----------

    public void setAlbedo(Texture texture) {
        this.albedo = texture;
    }

    public void setNormalMap(Texture texture) {
        this.normalMap = texture;
    }

    public void setMaterialUBO(UniformBuffer ubo) {
        this.materialUBO = ubo;
    }

    public Material setTint(float r, float g, float b) {
        tint.set(r, g, b);
        return this;
    }

    // ---------- BIND ----------

    public void bindProperties() {
        shaderProgram.setInt("albedoTexture", 0);
//        shaderProgram.setInt("normalMap", 1);

        shaderProgram.setVec3("materialTint", tint.x, tint.y, tint.z);


        if (materialUBO != null) {
             materialUBO.bind();
        }
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    public Texture getAlbedo() {
        return albedo;
    }

    public Texture getNormalMap() {
        return normalMap;
    }

    public int getId() {
        return id;
    }
}