package org.myeslib.core;

import org.myeslib.data.Command;

public interface CommandHandler<C extends Command> {

    void handle(C command);

}
