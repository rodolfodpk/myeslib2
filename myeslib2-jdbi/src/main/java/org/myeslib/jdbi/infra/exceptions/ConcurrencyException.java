package org.myeslib.jdbi.infra.exceptions;

public class ConcurrencyException extends InfraRuntimeException {
    public ConcurrencyException(String s) {
        super(s);
    }
}
