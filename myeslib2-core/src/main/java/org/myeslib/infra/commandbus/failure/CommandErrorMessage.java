package org.myeslib.infra.commandbus.failure;

import org.myeslib.data.Command;

import java.util.Optional;

public interface CommandErrorMessage {

    CommandErrorMessageId getId();
    Command getCommand();
    Optional<String> getDescription();
}
