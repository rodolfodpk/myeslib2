package org.myeslib.stack1.infra.commandbus;

import org.myeslib.data.Command;
import org.myeslib.data.CommandId;

public class TestCommand implements Command {
    private final CommandId id;

    public TestCommand(CommandId id) {
        this.id = id;
    }

    @Override
    public CommandId getCommandId() {
        return id;
    }
}
