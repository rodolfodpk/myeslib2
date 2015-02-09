package org.myeslib.stack1.infra.exceptions;

public class ConcurrencyException extends InfraRuntimeException {
    public ConcurrencyException(String s) {
        super(s);
    }
}
