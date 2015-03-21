package org.myeslib.infra.failure;

import net.jcip.annotations.Immutable;

import java.util.UUID;

@Immutable
public class CommandErrorMessageId {

    private final UUID uuid;

    public CommandErrorMessageId(UUID uuid) {
        this.uuid =  uuid;
    }

    public UUID uuid() {
        return uuid;
    }

    public static CommandErrorMessageId create(UUID uuid) {
        return new CommandErrorMessageId(uuid);
    }

    public static CommandErrorMessageId create() {
        return new CommandErrorMessageId(UUID.randomUUID());
    }

    public String toString() {
        return uuid().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandErrorMessageId commandId = (CommandErrorMessageId) o;

        if (!uuid.equals(commandId.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
