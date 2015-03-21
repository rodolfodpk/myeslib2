package org.myeslib.infra.failure;

import org.myeslib.data.Command;

import java.util.Optional;

public interface CommandErrorMessage {

    CommandErrorMessageId getId();
    Command getCommand();
    Optional<String> getDescription();
}
