package com.crystal.engine.audio;

import com.crystal.engine.core.Disposable;

import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

public final class SoundBuffer implements Disposable {

    private final int id;

    public SoundBuffer(ShortBuffer samples, int channels, int sampleRate) {
        if (samples == null) throw new IllegalArgumentException("Samples cannot be null");
        if (channels != 1 && channels != 2)
            throw new IllegalArgumentException("Only mono and stereo sounds are supported");
        if (sampleRate <= 0) throw new IllegalArgumentException("Sample rate must be greater than 0");

        id = alGenBuffers();

        int format = channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
        alBufferData(id, format, samples, sampleRate);
    }

    public int getId() {
        return id;
    }

    @Override
    public void dispose() {
        alDeleteBuffers(id);
    }
}
