package com.crystal.engine.render;

public final class RenderLayers {
    public static final int WORLD = 1;  // 1 << 0
    public static final int UI = 1 << 1;
    public static final int DEBUG = 1 << 2;
    public static final int EDITOR = 1 << 3;

    public static final int ALL = -1;

    private RenderLayers() {}

    public static int fromName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Layer name cannot be null or blank");

        return switch (name) {
            case "world" -> WORLD;
            case "ui" -> UI;
            case "debug" -> DEBUG;
            case "editor" -> EDITOR;
            default -> throw new IllegalArgumentException("Unknown render layer: " + name);
        };
    }

    public static String describe(int mask) {
        StringBuilder description = new StringBuilder();

        append(description, mask, RenderLayers.WORLD, "World");
        append(description, mask, RenderLayers.UI, "UI");
        append(description, mask, RenderLayers.DEBUG, "Debug");
        append(description, mask, RenderLayers.EDITOR, "Editor");

        if (description.isEmpty())
            return "Custom";

        return description.toString();
    }

    private static void append(StringBuilder description, int mask, int layer, String name) {
        if ((mask & layer) == 0)
            return;

        if (!description.isEmpty())
            description.append(", ");

        description.append(name);
    }
}
