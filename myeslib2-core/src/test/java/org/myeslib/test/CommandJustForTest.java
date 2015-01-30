package org.myeslib.test;

import org.myeslib.core.Command;

import java.util.UUID;

@SuppressWarnings("serial")
public class CommandJustForTest implements Command<UUID> {

    private final UUID commandId;
    private final UUID id;

    public CommandJustForTest(UUID commandId, UUID id) {
        this.commandId = commandId;
        this.id = id;
    }

    public UUID getCommandId() {
        return commandId;
    }

    @Override
    public UUID getTargetId() {
        return id;
    }

}
