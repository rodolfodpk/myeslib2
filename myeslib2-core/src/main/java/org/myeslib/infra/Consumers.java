package org.myeslib.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.EventMessage;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Consumers<E extends EventSourced> {

    private static final Logger log = LoggerFactory.getLogger(Consumers.class);

    private final List<Consumer<List<EventMessage>>> eventMessageConsumers;
    private final List<Consumer<CommandErrorMessage>> errorMessageConsumers;
    private final List<Consumer<List<Command>>> commandsConsumers;

    public Consumers() {
        this.eventMessageConsumers = new ArrayList<>();
        this.errorMessageConsumers = new ArrayList<>();
        this.commandsConsumers = new ArrayList<>();
    }

    public Consumers<E> withEventsConsumer(Consumer<List<EventMessage>> eventsConsumer) {
        eventMessageConsumers.add(eventsConsumer);
        return this;
    }

    public Consumers<E> withErrorConsumer(Consumer<CommandErrorMessage> errorConsumer) {
        errorMessageConsumers.add(errorConsumer);
        return this;
    }

    public Consumers<E> withCommandrConsumer(Consumer<List<Command>> commandsConsumer) {
        commandsConsumers.add(commandsConsumer);
        return this;
    }

    public void consumeEvents(List<EventMessage> eventMessages) {
        for (Consumer<List<EventMessage>> eventsConsumer : eventMessageConsumers) {
            log.warn("propagating event messages {} to consumers", eventMessages);
            eventsConsumer.accept(eventMessages);
        }
    }

    public void consumeError(CommandErrorMessage commandErrorMessage) {
        for (Consumer<CommandErrorMessage> commandErrorMessageConsumer : errorMessageConsumers) {
            log.warn("propagating error message {} to consumers", commandErrorMessage);
            commandErrorMessageConsumer.accept(commandErrorMessage);
        }
    }

    public void consumeCommands(List<Command> commands) {
        for (Consumer<List<Command>> commandsConsumer : commandsConsumers) {
            log.warn("propagating commands {} to consumers", commands);
            commandsConsumer.accept(commands);
        }
    }

}
