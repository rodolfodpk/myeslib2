package org.myeslib.core;

import org.myeslib.data.Command;
import org.myeslib.data.Event;
import org.myeslib.infra.SagaInteractionContext;

import java.util.List;

public interface Saga extends EventSourced {

    SagaInteractionContext getInteractionContext();

    void setInteractionContext(SagaInteractionContext interactionContext);

    default void emit(Event event) {
        getInteractionContext().emit(event);
    }

    default List<Event> getEmitedvents() {
        return getInteractionContext().getEmittedEvents();
    }

    default void emit(Command command) {
        getInteractionContext().emit(command);
    }

    default List<Command> getEmitedCommands() {
        return getInteractionContext().getEmittedCommands();
    }

}
