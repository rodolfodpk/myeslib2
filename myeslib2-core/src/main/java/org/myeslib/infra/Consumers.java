package org.myeslib.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.EventMessage;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public interface Consumers<E extends EventSourced> {

    default List<Consumer<List<EventMessage>>> eventMessageConsumers() {
        return Collections.emptyList();
    }

    default List<Consumer<List<Command>>> commandsConsumers() {
        return Collections.emptyList();
    }

    default List<Consumer<CommandErrorMessage>> errorMessageConsumers() {
        return Collections.emptyList();
    }

}
