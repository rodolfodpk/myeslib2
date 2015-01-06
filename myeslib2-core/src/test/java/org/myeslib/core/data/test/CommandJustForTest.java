package org.myeslib.core.data.test;

import java.util.UUID;

import org.myeslib.core.Command;

@SuppressWarnings("serial")
public class CommandJustForTest implements Command{

    private final UUID commandId;
    private final UUID id;
    private final Long targetVersion;

    public CommandJustForTest(UUID commandId, UUID id, Long targetVersion) {
        this.commandId = commandId;
        this.id = id;
        this.targetVersion = targetVersion;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public UUID getId() {
        return id;
    }

    public Long getTargetVersion() {
        return targetVersion;
    }
}
