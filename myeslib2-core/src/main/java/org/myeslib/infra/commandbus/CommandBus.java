package org.myeslib.infra.commandbus;

import org.myeslib.data.Command;

public interface CommandBus {

    void post(Command command);
}
