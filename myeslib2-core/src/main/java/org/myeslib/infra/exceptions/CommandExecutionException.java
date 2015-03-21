package org.myeslib.infra.exceptions;

public class CommandExecutionException extends RuntimeException {
    public CommandExecutionException(String s) {
        super(s);
    }
}
