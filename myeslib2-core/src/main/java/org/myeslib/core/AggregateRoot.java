package org.myeslib.core;

import org.myeslib.data.Event;
import org.myeslib.infra.InteractionContext;

import java.io.Serializable;
import java.util.List;

public interface AggregateRoot extends Serializable {

    InteractionContext getInteractionContext();

    default List<? extends Event> getAppliedEvents() {
       return getInteractionContext().getAppliedEvents();
    }

    void setInteractionContext(InteractionContext interactionContext);
}
