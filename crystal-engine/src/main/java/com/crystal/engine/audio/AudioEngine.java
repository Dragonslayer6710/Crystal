package com.crystal.engine.audio;

import com.crystal.engine.core.Disposable;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import static org.lwjgl.openal.ALC10.*;

public final class AudioEngine implements Disposable {

    private long device;
    private long context;
    private boolean initialized;

    public void init() {
        if (initialized)
            return;

        device = alcOpenDevice((String) null);
        if (device == 0)
            throw new IllegalStateException("Failed to open default OpenAL device");

        context = alcCreateContext(device, (int[]) null);
        if (context == 0)
            throw new IllegalStateException("Failed to create OpenAL context");

        alcMakeContextCurrent(context);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        AL.createCapabilities(alcCapabilities);

        initialized = true;
    }

    @Override
    public void dispose() {
        if (!initialized)
            return;

        alcMakeContextCurrent(0);

        if (context != 0) {
            alcDestroyContext(context);
            context = 0;
        }

        if (device != 0) {
            alcCloseDevice(device);
            device = 0;
        }

        initialized = false;
    }
}
