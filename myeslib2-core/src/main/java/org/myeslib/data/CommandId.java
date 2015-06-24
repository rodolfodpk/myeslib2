package org.myeslib.data;


import java.io.Serializable;
import java.util.UUID;

public class CommandId implements Serializable {

    private final UUID uuid;

    public CommandId(UUID uuid) {
        this.uuid =  uuid;
    }

    public UUID uuid() {
        return uuid;
    }

    public static CommandId create(UUID uuid) {
        return new CommandId(uuid);
    }

    public static CommandId create() {
        return new CommandId(UUID.randomUUID());
    }

    public String toString() {
        return uuid().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandId commandId = (CommandId) o;

        if (!uuid.equals(commandId.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
