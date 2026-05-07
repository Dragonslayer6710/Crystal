package com.crystal.engine.render.gl;

import com.crystal.engine.core.Disposable;

public abstract class GLObject implements Disposable {

    protected final int id;

    private boolean disposed;

    protected GLObject(int id) {
        this.id = id;
    }

    public final int getId() {
        return id;
    }

    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public final void dispose() {
        if (disposed) return;

        disposeInternal();

        disposed = true;
    }

    protected abstract void disposeInternal();
}
