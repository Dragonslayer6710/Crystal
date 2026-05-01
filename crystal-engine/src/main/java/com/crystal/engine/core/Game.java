package com.crystal.engine.core;

public interface Game {
    void init();
    void update(double deltaTime);
    void render();
    void shutdown();
}
