package org.myeslib.infra;

import org.myeslib.data.Command;

import java.util.List;

public interface SagaInteractionContext extends InteractionContext {

    void emit(Command command);

    List<Command> getEmittedCommands();

}
