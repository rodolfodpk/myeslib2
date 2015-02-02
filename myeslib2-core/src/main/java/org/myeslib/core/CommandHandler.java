package org.myeslib.core;

public interface CommandHandler<C extends Command> {

    void handle(C command);

}
