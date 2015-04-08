package org.myeslib.infra.commandbus;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;

public interface CommandBus<E extends EventSourced> {

    void post(Command command);
}
