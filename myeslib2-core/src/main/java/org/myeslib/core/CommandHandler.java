package org.myeslib.core;

import net.jcip.annotations.ThreadSafe;
import org.myeslib.data.Command;

@ThreadSafe
public interface CommandHandler<C extends Command> {

    void handle(C command);

}
