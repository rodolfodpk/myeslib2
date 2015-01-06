package org.myeslib.core;

import org.myeslib.core.data.UnitOfWork;

public interface CommandHandler<C extends Command> {

    UnitOfWork handle(C command);

}
