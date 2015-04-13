package org.myeslib.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.EventMessage;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;

import java.util.List;
import java.util.function.Consumer;

public interface Consumers<E extends EventSourced> {

    List<Consumer<List<EventMessage>>> eventMessageConsumers();

    List<Consumer<List<Command>>> commandsConsumers();

    List<Consumer<CommandErrorMessage>> errorMessageConsumers();

}
