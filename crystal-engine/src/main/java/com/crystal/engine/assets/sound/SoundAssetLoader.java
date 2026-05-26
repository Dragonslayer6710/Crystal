package com.crystal.engine.assets.sound;

import com.crystal.engine.audio.SoundBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBVorbis.*;

public final class SoundAssetLoader {

    private SoundAssetLoader() {}

    public static SoundBuffer loadOgg(Path path) {
        if (path == null) throw new IllegalArgumentException("Path cannot be null");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer channels = stack.mallocInt(1);
            IntBuffer sampleRate = stack.mallocInt(1);

            ShortBuffer samples = stb_vorbis_decode_filename(path.toString(), channels, sampleRate);

            if (samples == null) throw new IllegalArgumentException("Failed to load OGG sound: " + path);

            try {
                return new SoundBuffer(samples, channels.get(0), sampleRate.get(0));
            } finally {
                MemoryUtil.memFree(samples);
            }
        }
    }
}
