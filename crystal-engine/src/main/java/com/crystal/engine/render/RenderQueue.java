package com.crystal.engine.render;

import com.crystal.engine.render.command.RenderCommand;
import com.crystal.engine.render.command.RenderCommandContext;

import java.util.ArrayList;
import java.util.List;

final class RenderQueue {

    private final List<RenderCommand> commands = new ArrayList<>();

    void submit(RenderCommand command) {
        commands.add(command);
    }

    void execute(RenderCommandContext context) {
        for (RenderCommand command : commands) {
            command.execute(context);
        }
    }

    void clear() {
        commands.clear();
    }
}
