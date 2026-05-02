package com.crystal.engine.render;

import java.util.ArrayList;
import java.util.List;

public class RenderQueue {

    private final List<RenderCommand> commands = new ArrayList<>();

    public void submit(RenderCommand command) {
        commands.add(command);
    }

    public void execute() {
        for (RenderCommand command : commands) {
            command.execute();
        }
    }

    public void clear() {
        commands.clear();
    }
}
