package com.crystal.engine.render.texture;

import com.crystal.engine.core.Disposable;
import com.crystal.engine.render.GLObjectLabel;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;

public class Texture implements Disposable {

    private final int id;
    private final int width;
    private final int height;
    private final String sourcePath;

    public Texture(int id, int width, int height, String sourcePath) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.sourcePath = sourcePath;
    }

    public static Texture create1x1(String name, int r, int g, int b, int a) {
        int textureId = glCreateTextures(GL_TEXTURE_2D);

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

        return new Texture(textureId, 1, 1, label);
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void bind(int texture, int target) {
        glActiveTexture(texture);
        glBindTexture(target, id);
    }

    @Override
    public void dispose() {
         glDeleteTextures(id);
    }
}
