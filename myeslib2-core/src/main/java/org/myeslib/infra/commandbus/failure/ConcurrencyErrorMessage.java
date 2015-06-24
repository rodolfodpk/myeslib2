package org.myeslib.infra.commandbus.failure;

import org.myeslib.data.Command;

import java.util.Optional;

public class ConcurrencyErrorMessage implements CommandErrorMessage {

    private final CommandErrorMessageId id;
    private final Command command;

    public ConcurrencyErrorMessage(CommandErrorMessageId id, Command command) {
        this.id = id;
        this.command = command;
    }

    @Override
    public CommandErrorMessageId getId() {
        return id;
    }

    @Override
    public Command getCommand() {
        return command;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConcurrencyErrorMessage that = (ConcurrencyErrorMessage) o;

        if (!command.equals(that.command)) return false;
        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + command.hashCode();
        return result;
    }
}
