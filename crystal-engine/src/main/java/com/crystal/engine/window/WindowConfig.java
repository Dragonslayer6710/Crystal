package com.crystal.engine.window;

public record WindowConfig(
        int width,
        int height,
        String title,
        boolean resizeable,
        boolean vSync,
        boolean debugContext
) {
    public static WindowConfig defaults() {
        return new WindowConfig(
                1280,
                720,
                "Crystal Engine",
                true,
                false,
                true
        );
    }
}
