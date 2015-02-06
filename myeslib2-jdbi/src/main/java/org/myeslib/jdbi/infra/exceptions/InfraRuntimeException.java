package org.myeslib.jdbi.infra.exceptions;

public class InfraRuntimeException extends RuntimeException {
    public InfraRuntimeException(String s) {
        super(s);
    }
}
