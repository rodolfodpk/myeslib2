package org.myeslib.infra.exceptions;

public class CommandExecutionException extends RuntimeException {
    public CommandExecutionException(Throwable cause) {
        super(cause);
    }
}
