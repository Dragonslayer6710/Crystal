package com.crystal.engine.render.texture;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.stb.STBImage.*;

public class TextureLoader {
    private TextureLoader() {}

    public static Texture load(Path path) {
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
                    1,
                    GL_RGBA8,
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

            glTextureParameteri(textureId, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTextureParameteri(textureId, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTextureParameteri(textureId, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTextureParameteri(textureId, GL_TEXTURE_WRAP_T, GL_REPEAT);

            stbi_image_free(pixels);

            return new Texture(textureId);
        }
    }
}
