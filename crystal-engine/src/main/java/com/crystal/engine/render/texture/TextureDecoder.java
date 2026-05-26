package com.crystal.engine.render.texture;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBImage.*;

public class TextureDecoder {

    private TextureDecoder() {}

    public static Texture load(Path path, TextureSettings settings) {
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        settings.validate();

        stbi_set_flip_vertically_on_load(true);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);
            var channels = stack.mallocInt(1);

            ByteBuffer pixels = stbi_load(
                    path.toString(),
                    width,
                    height,
                    channels,
                    4
            );

            if (pixels == null)
                throw new RuntimeException("Failed to load texture: " + path + "\n" + stbi_failure_reason());

            Texture texture = TextureFactory.createTextureFromPixels(
                    pixels,
                    width.get(0),
                    height.get(0),
                    settings,
                    path.toString()
            );

            stbi_image_free(pixels);
            return texture;
        }
    }

    public static Texture load(Path path) {
        return load(path, new TextureSettings());
    }

    public static Texture load(Path path, TextureFormat format) {
        return load(path, new TextureSettings().setFormat(format));
    }

    public static Texture loadFromMemory(ByteBuffer encodedImage, TextureSettings settings, String sourcePath) {
        if (encodedImage == null) throw new IllegalArgumentException("Encoded image cannot be null");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        settings.validate();
        stbi_set_flip_vertically_on_load(true);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);
            var channels = stack.mallocInt(1);

            ByteBuffer pixels = stbi_load_from_memory(encodedImage, width, height, channels, 4);

            if (pixels == null)
                throw new RuntimeException("Failed to load embedded texture: "
                        + sourcePath + "\n" + stbi_failure_reason());

            Texture texture = TextureFactory.createTextureFromPixels(
                    pixels,
                    width.get(0),
                    height.get(0),
                    settings,
                    sourcePath
            );

            stbi_image_free(pixels);
            return texture;
        }
    }

    public static Texture loadHDR(Path path, TextureSettings settings) {
        if (path == null) throw new IllegalArgumentException("Path cannot be null");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

        settings.validate();

        stbi_set_flip_vertically_on_load(true);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);
            var channels = stack.mallocInt(1);

            FloatBuffer pixels = stbi_loadf(
                    path.toString(),
                    width,
                    height,
                    channels,
                    4
            );

            if (pixels == null)
                throw new RuntimeException("Failed to load HDR texture: " + path + "\n" + stbi_failure_reason());

            Texture texture = TextureFactory.createTextureFromFloatPixels(
                    pixels,
                    width.get(0),
                    height.get(0),
                    settings,
                    path.toString()
            );

            stbi_image_free(pixels);

            return texture;
        }
    }

    public static Texture loadHDR(Path path) {
        return loadHDR(path, TextureSettings.defaultHDR());
    }

}
