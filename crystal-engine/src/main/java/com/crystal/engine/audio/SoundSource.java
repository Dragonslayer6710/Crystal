package com.crystal.engine.audio;

import com.crystal.engine.core.Disposable;

import static org.lwjgl.openal.AL10.*;

public final class SoundSource implements Disposable {
    private final int id;

    public SoundSource() {
        id = alGenSources();
    }

    public SoundSource setBuffer(SoundBuffer buffer) {
        if (buffer == null) throw new IllegalArgumentException("SoundBuffer cannot be null");

        alSourcei(id, AL_BUFFER, buffer.getId());
        return this;
    }

    public void play() {
        alSourcePlay(id);
    }

    public void stop() {
        alSourceStop(id);
    }

    public boolean isPlaying() {
        return alGetSourcei(id, AL_SOURCE_STATE) == AL_PLAYING;
    }

    @Override
    public void dispose() {
        alDeleteSources(id);
    }
}
