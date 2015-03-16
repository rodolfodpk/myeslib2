package org.myeslib.core;

import org.myeslib.data.Command;
import org.myeslib.data.Event;
import org.myeslib.infra.SagaInteractionContext;

import java.io.Serializable;
import java.util.List;

public interface Saga extends Serializable {

    SagaInteractionContext getInteractionContext();

    default List<? extends Event> getAppliedEvents() {
        return getInteractionContext().getAppliedEvents();
    }

    default List<? extends Command> getEmitedCommands() {
        return getInteractionContext().getEmitedCommands();
    }

    void setInteractionContext(SagaInteractionContext interactionContext);

}
