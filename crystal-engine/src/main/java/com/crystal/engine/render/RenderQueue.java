package com.crystal.engine.render;

import com.crystal.engine.render.commands.RenderCommand;

import java.util.ArrayList;
import java.util.List;

final class RenderQueue {

    private final List<RenderCommand> commands = new ArrayList<>();

    void submit(RenderCommand command) {
        commands.add(command);
    }

    void execute(RenderContext context) {
        for (RenderCommand command : commands) {
            command.execute(context);
        }
    }

    void clear() {
        commands.clear();
    }
}
