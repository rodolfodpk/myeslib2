package org.myeslib.infra.commandbus.failure;

import org.myeslib.data.Command;

import java.util.Optional;

public class UnknowErrorMessage implements CommandErrorMessage {

    private final CommandErrorMessageId id;
    private final Command command;
    private final String description;

    public UnknowErrorMessage(CommandErrorMessageId id, Command command, String description) {
        this.id = id;
        this.command = command;
        this.description = description;
    }

    public CommandErrorMessageId getId() {
        return id;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of(description);
    }

    @Override
    public String toString() {
        return "CmdExecErrorMessage{" +
                "id=" + id +
                ", command=" + command +
                ", description=" + description +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnknowErrorMessage that = (UnknowErrorMessage) o;

        if (!command.equals(that.command)) return false;
        if (!description.equals(that.description)) return false;
        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + command.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }

}
