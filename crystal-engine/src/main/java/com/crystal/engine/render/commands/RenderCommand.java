package com.crystal.engine.render.commands;

import com.crystal.engine.render.RenderContext;

public interface RenderCommand {
    void execute(RenderContext context);
}
