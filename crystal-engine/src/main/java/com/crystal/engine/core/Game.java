package com.crystal.engine.core;

public interface Game {
    void init(EngineContext ctx);
    void update(double deltaTime);
    void shutdown();
}
