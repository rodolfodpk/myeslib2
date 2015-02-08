package org.myeslib.core;

import java.util.UUID;

public class CommandId {

    private final UUID commandId;

    @Override
    public String toString() {
        return commandId.toString();
    }

    public CommandId(UUID commandId) {
        this.commandId = commandId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandId commandId1 = (CommandId) o;

        if (!commandId.equals(commandId1.commandId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return commandId.hashCode();
    }
}
