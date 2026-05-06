package com.crystal.engine.render;

import com.crystal.engine.render.commands.RenderCommand;

import java.util.ArrayList;
import java.util.List;

public class RenderQueue {

    private final List<RenderCommand> commands = new ArrayList<>();

    public void submit(RenderCommand command) {
        commands.add(command);
    }

    public void execute(RenderContext context) {
        for (RenderCommand command : commands) {
            command.execute(context);
        }
    }

    public void clear() {
        commands.clear();
    }
}
