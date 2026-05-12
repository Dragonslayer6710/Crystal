package com.crystal.engine.render.texture;

import com.crystal.engine.graphics.TextureSettings;
import com.crystal.engine.graphics.TextureTarget;
import com.crystal.engine.graphics.TextureWrap;
import com.crystal.engine.render.GLObjectLabel;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL45.*;

public final class TextureFactory {

    private TextureFactory() {}

    private static int mipLevels(int width, int height) {
        return 1 + (int) Math.floor(
                Math.log(Math.max(width, height)) / Math.log(2)
        );
    }

    public static Texture create1x1(String name, int r, int g, int b, int a) {
        int textureId = glCreateTextures(TextureTarget.TEXTURE_2D.glValue);

        glTextureStorage2D(textureId, 1, GL_RGBA8, 1, 1);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer pixel = stack.malloc(4);

            pixel.put((byte) r);
            pixel.put((byte) g);
            pixel.put((byte) b);
            pixel.put((byte) a);
            pixel.flip();

            glTextureSubImage2D(
                    textureId,
                    0,
                    0,
                    0,
                    1,
                    1,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    pixel
            );
        }

        glTextureParameteri(textureId, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTextureParameteri(textureId, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_T, GL_REPEAT);

        String label = "<generated:" + name + ">";
        GLObjectLabel.labelTexture(textureId, label);

        return new Texture(textureId, TextureTarget.TEXTURE_2D, 1, 1, 1, label);
    }

    static Texture createTextureFromPixels(ByteBuffer pixels, int width, int height,
                                           TextureSettings settings, String sourcePath) {
        int textureId = glCreateTextures(GL_TEXTURE_2D);
        GLObjectLabel.labelTexture(textureId, sourcePath);

        int levels = settings.isGenerateMipmaps() ? mipLevels(width, height) : 1;

        glTextureStorage2D(
                textureId,
                levels,
                settings.getFormat().glValue,
                width,
                height
        );

        glTextureSubImage2D(
                textureId,
                0,
                0,
                0,
                width,
                height,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                pixels
        );

        glTextureParameteri(textureId, GL_TEXTURE_MIN_FILTER, settings.getMinFilter().glValue);
        glTextureParameteri(textureId, GL_TEXTURE_MAG_FILTER, settings.getMagFilter().glValue);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_S, settings.getWrapS().glValue);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_T, settings.getWrapT().glValue);

        if (settings.isGenerateMipmaps())
            glGenerateTextureMipmap(textureId);

        return new Texture(textureId, TextureTarget.TEXTURE_2D, width, height, levels, sourcePath);
    }

    public static Texture createCubemap(int size, TextureSettings settings, String debugName) {
        if (size <= 0) throw new IllegalArgumentException("Cubemap size must be greater than 0");
        if (settings == null) throw new IllegalArgumentException("TextureSettings cannot be null");
        if (debugName == null || debugName.isBlank()) throw new IllegalArgumentException("Debug name cannot be null or blank");

        settings.validate();

        int textureId = glCreateTextures(TextureTarget.CUBE_MAP.glValue);
        GLObjectLabel.labelTexture(textureId, debugName);

        int levels = settings.isGenerateMipmaps() ? mipLevels(size, size) : 1;

        glTextureStorage2D(
                textureId,
                levels,
                settings.getFormat().glValue,
                size,
                size
        );

        glTextureParameteri(textureId, GL_TEXTURE_MIN_FILTER, settings.getMinFilter().glValue);
        glTextureParameteri(textureId, GL_TEXTURE_MAG_FILTER, settings.getMagFilter().glValue);

        glTextureParameteri(textureId, GL_TEXTURE_WRAP_S, TextureWrap.CLAMP_TO_EDGE.glValue);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_T, TextureWrap.CLAMP_TO_EDGE.glValue);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_R, TextureWrap.CLAMP_TO_EDGE.glValue);



        return new Texture(
                textureId,
                TextureTarget.CUBE_MAP,
                size,
                size,
                levels,
                debugName
        );
    }

    public static Texture createCubemap(int size, String debugName) {
        return createCubemap(size, TextureSettings.defaultCubemap(), debugName);
    }

    public static Texture createSolidCubemap(String name, int size, int r, int g, int b, int a) {
        if (size <= 0) throw new IllegalArgumentException("Cubemap size must be greater than 0");

        Texture texture = createCubemap(size, TextureSettings.defaultCubemap(), "<generated:" + name + ">");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer pixels = stack.malloc(size * size * 4);

            for (int i = 0; i < size * size; i++) {
                pixels.put((byte) r);
                pixels.put((byte) g);
                pixels.put((byte) b);
                pixels.put((byte) a);
            }

            pixels.flip();

            for (int face = 0; face < 6; face++) {
                glTextureSubImage3D(
                        texture.getId(),
                        0,
                        0,
                        0,
                        face,
                        size,
                        size,
                        1,
                        GL_RGBA,
                        GL_UNSIGNED_BYTE,
                        pixels
                );

                pixels.rewind();
            }
        }

        return texture;
    }

    public static Texture createTextureFromFloatPixels(FloatBuffer pixels, int width, int height,
                                                       TextureSettings settings, String sourcePath) {
        int textureId = glCreateTextures(TextureTarget.TEXTURE_2D.glValue);
        GLObjectLabel.labelTexture(textureId, sourcePath);

        int levels = settings.isGenerateMipmaps() ? mipLevels(width, height) : 1;

        glTextureStorage2D(
                textureId,
                levels,
                settings.getFormat().glValue,
                width,
                height
        );

        glTextureSubImage2D(
                textureId,
                0,
                0,
                0,
                width,
                height,
                GL_RGBA,
                GL_FLOAT,
                pixels
        );

        glTextureParameteri(textureId, GL_TEXTURE_MIN_FILTER, settings.getMinFilter().glValue);
        glTextureParameteri(textureId, GL_TEXTURE_MAG_FILTER, settings.getMagFilter().glValue);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_S, settings.getWrapS().glValue);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_T, settings.getWrapT().glValue);

        if (settings.isGenerateMipmaps()) {
            glGenerateTextureMipmap(textureId);
        }

        return new Texture(
                textureId,
                TextureTarget.TEXTURE_2D,
                width,
                height,
                levels,
                sourcePath
        );
    }

    public static Texture createRenderTexture2D(int width, int height, TextureSettings settings, String debugName) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Texture size must be greater than 0");

        if (settings == null)
            throw new IllegalArgumentException("TextureSettings cannot be null");

        if (debugName == null || debugName.isBlank())
            throw new IllegalArgumentException("Debug name cannot be null or blank");

        settings.validate();

        int textureId = glCreateTextures(TextureTarget.TEXTURE_2D.glValue);
        GLObjectLabel.labelTexture(textureId, debugName);

        int levels = settings.isGenerateMipmaps() ? mipLevels(width, height) : 1;

        glTextureStorage2D(
                textureId,
                levels,
                settings.getFormat().glValue,
                width,
                height
        );

        glTextureParameteri(textureId, GL_TEXTURE_MIN_FILTER, settings.getMinFilter().glValue);
        glTextureParameteri(textureId, GL_TEXTURE_MAG_FILTER, settings.getMagFilter().glValue);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_S, settings.getWrapS().glValue);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_T, settings.getWrapT().glValue);

        return new Texture(
                textureId,
                TextureTarget.TEXTURE_2D,
                width,
                height,
                levels,
                debugName
        );
    }
}
