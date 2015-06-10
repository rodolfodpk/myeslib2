package org.myeslib.core;

import org.myeslib.data.Command;
import org.myeslib.data.CommandId;
import org.myeslib.data.Event;
import org.myeslib.infra.SagaInteractionContext;

import java.time.Instant;
import java.util.List;

public interface Saga extends EventSourced {

    SagaInteractionContext getInteractionContext();

    default CommandId getCommandId() {
        return getInteractionContext().getCommandId();
    }

    default List<Event> getEmittedEvents() {
        return getInteractionContext().getEmittedEvents();
    }

    // session methods

    void setInteractionContext(SagaInteractionContext interactionContext);

    default void setCommandId(CommandId commandId) {
        getInteractionContext().setCommandId(commandId);
    }

    default void emit(Event event) {
        getInteractionContext().emit(event);
    }

    default void emitCommand(Command command) {
        getInteractionContext().emitCommand(command);
    }

    default void scheduleCommand(Command command) {
        getInteractionContext().scheduleCommand(new SagaInteractionContext.CommandSchedule(getCommandId(), command));
    }

    default void scheduleCommand(Command command, Instant instant) {
        getInteractionContext().scheduleCommand(new SagaInteractionContext.CommandSchedule(getCommandId(), command, instant));
    }

    default void emitSideEffect(String description, Runnable runnable) {
        getInteractionContext().emitSideEffect(new SagaInteractionContext.RunnableSideEffect(getCommandId(), description, runnable));
    }

    default void processSideEffects() {
        getInteractionContext().processSideEffects();
    }
}
