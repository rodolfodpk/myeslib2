package org.myeslib.stack1.infra.commandbus;

import org.myeslib.core.EventSourced;
import org.myeslib.infra.commandbus.CommandSubscriber;

public interface TestCommandSubscriber<E extends EventSourced> extends CommandSubscriber<E> {

    public void on(TestCommand command);
}
