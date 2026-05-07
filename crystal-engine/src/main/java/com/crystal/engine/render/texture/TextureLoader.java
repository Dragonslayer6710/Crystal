package com.crystal.engine.render.texture;

import com.crystal.engine.graphics.TextureFormat;
import com.crystal.engine.graphics.TextureSettings;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.stb.STBImage.*;

public class TextureLoader {
    private TextureLoader() {}

    private static int mipLevels(int width, int height) {
        return 1 + (int) Math.floor(
                Math.log(Math.max(width, height)) / Math.log(2)
        );
    }

    public static Texture load(Path path, TextureSettings settings) {
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");

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

            int textureId = glCreateTextures(GL_TEXTURE_2D);

            glTextureStorage2D(
                    textureId,
                    settings.isGenerateMipmaps() ? mipLevels(width.get(0), height.get(0)) : 1,
                    settings.getFormat().glValue,
                    width.get(0),
                    height.get(0)
            );

            glTextureSubImage2D(
                    textureId,
                    0,
                    0,
                    0,
                    width.get(0),
                    height.get(0),
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    pixels
            );

            glTextureParameteri(textureId, GL_TEXTURE_MIN_FILTER, settings.getMinFilter().glValue);
            glTextureParameteri(textureId, GL_TEXTURE_MAG_FILTER, settings.getMagFilter().glValue);
            glTextureParameteri(textureId, GL_TEXTURE_WRAP_S, settings.getWrapS().glValue);
            glTextureParameteri(textureId, GL_TEXTURE_WRAP_T, settings.getWrapT().glValue);

            stbi_image_free(pixels);

            return new Texture(textureId);
        }
    }

    public static Texture load(Path path) {
        return load(path, new TextureSettings());
    }

    public static Texture load(Path path, TextureFormat format) {
        return load(path, new TextureSettings().setFormat(format));
    }
}
