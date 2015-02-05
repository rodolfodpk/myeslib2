package org.myeslib.core;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public interface CommandHandler<C extends Command> {

    void handle(C command);

}
