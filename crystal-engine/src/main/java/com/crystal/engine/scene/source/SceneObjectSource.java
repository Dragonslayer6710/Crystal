package com.crystal.engine.scene.source;

public final class SceneObjectSource {

    public enum Type {
        EMPTY,
        PRIMITIVE,
        MODEL
    }

    private final Type type;
    private final String path;
    private final String primitive;
    private final String material;

    private SceneObjectSource(Type type, String path, String primitive, String material) {
        if (type == null) throw new IllegalArgumentException("Source type cannot be null");

        this.type = type;
        this.path = path;
        this.primitive = primitive;
        this.material = material;
    }

    public static SceneObjectSource empty() {
        return new SceneObjectSource(Type.EMPTY, null, null, null);
    }

    public static SceneObjectSource primitive(String primitive, String material) {
        if (primitive == null || primitive.isBlank())
            throw new IllegalArgumentException("Primitive cannot be null or blank");

        return new SceneObjectSource(Type.PRIMITIVE, null, primitive, material);
    }

    public static SceneObjectSource model(String path) {
        if (path == null || path.isBlank())
            throw new IllegalArgumentException("Model path cannot be null or blank");

        return new SceneObjectSource(Type.MODEL, path, null, null);
    }

    public Type getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getPrimitive() {
        return primitive;
    }

    public String getMaterial() {
        return material;
    }
}
