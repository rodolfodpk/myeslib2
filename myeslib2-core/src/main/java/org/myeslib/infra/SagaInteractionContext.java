package org.myeslib.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

public interface SagaInteractionContext<E extends EventSourced> extends InteractionContext {

    CommandId getCommandId();

    void setCommandId(CommandId commandId);

    void emitCommand(Command command);

    void scheduleCommand(CommandSchedule command);

    void emitSideEffect(RunnableSideEffect runnableSideEffect);

    void processSideEffects();

    class CommandSchedule implements Serializable{

        private final CommandId commandId;
        private final Command command;
        private final Optional<Instant> instant;

        public CommandSchedule(CommandId commandId, Command command, Instant instant) {
            this.commandId = commandId;
            this.command = command;
            this.instant = Optional.of(instant);
        }

        public CommandSchedule(CommandId commandId, Command command) {
            this.commandId = commandId;
            this.command = command;
            this.instant = Optional.empty();
        }

        public Command getCommand() {
            return command;
        }

        public Optional<Instant> getInstant() {
            return instant;
        }

        public CommandId getCommandId() {
            return commandId;
        }
    }

    class RunnableSideEffect implements Serializable{

        private final CommandId commandId;
        private final String description;
        private final Runnable runnable;

        public RunnableSideEffect(CommandId commandId, String description, Runnable runnable) {
            this.commandId = commandId;
            this.description = description;
            this.runnable = runnable;
        }

        public String getDescription() {
            return description;
        }

        public CommandId getCommandId() {
            return commandId;
        }

        public void run() {
            runnable.run();
        }
    }

}
