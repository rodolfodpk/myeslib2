package org.myeslib.core;

import org.myeslib.data.Event;
import org.myeslib.infra.InteractionContext;

import java.util.List;

public interface AggregateRoot extends EventSourced {

    InteractionContext getInteractionContext();

    void setInteractionContext(InteractionContext interactionContext);

    default void emit(Event event) {
        getInteractionContext().emit(event);
    }

    default List<Event> getEmittedEvents() {
        return getInteractionContext().getEmittedEvents();
    }

}
