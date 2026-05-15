package com.crystal.engine.core.exception;

public class CrystalEngineException extends RuntimeException {

    public CrystalEngineException(String message) {
        super(message);
    }

    public CrystalEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
