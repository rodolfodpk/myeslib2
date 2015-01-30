package org.myeslib.data;

import org.myeslib.core.Command;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CommandResults<K> {

    private final Command<K> command;
    private final UnitOfWork unitOfWork;
    private final List<Command<K>> externalCommands;

    public CommandResults(Command<K> command, UnitOfWork unitOfWork) {
        this.command = command;
        this.unitOfWork = unitOfWork;
        this.externalCommands = Collections.emptyList();
    }

    public UUID getCommandId() { return command.getCommandId(); }

    public Command<K> getCommand() { return command; }

    public K getTargetId() {
        return command.getTargetId();
    }

    public UnitOfWork getUnitOfWork() {
        return unitOfWork;
    }

    public List<Command<K>> getExternalCommands() {
        return Collections.unmodifiableList(externalCommands);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandResults that = (CommandResults) o;

        if (command != null ? !command.equals(that.command) : that.command != null) return false;
        if (!externalCommands.equals(that.externalCommands)) return false;
        if (!unitOfWork.equals(that.unitOfWork)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = command != null ? command.hashCode() : 0;
        result = 31 * result + unitOfWork.hashCode();
        result = 31 * result + externalCommands.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CommandResults{" +
                "command=" + command +
                ", unitOfWork=" + unitOfWork +
                ", externalCommands=" + externalCommands +
                '}';
    }

}
