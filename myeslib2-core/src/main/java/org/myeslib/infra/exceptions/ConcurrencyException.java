package org.myeslib.infra.exceptions;

public class ConcurrencyException extends CommandExecutionException {

    private final Long newVersion;
    private final Long currentVersion;

    public ConcurrencyException(Throwable cause, Long newVersion, Long currentVersion) {
        super(cause);
        this.newVersion = newVersion;
        this.currentVersion = currentVersion;
    }

    public Long getNewVersion() {
        return newVersion;
    }

    public Long getCurrentVersion() {
        return currentVersion;
    }

}
