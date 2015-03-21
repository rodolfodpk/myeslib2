package org.myeslib.infra.exceptions;

public class ConcurrencyException extends CommandExecutionException {
    public ConcurrencyException(String s) {
        super(s);
    }
}
