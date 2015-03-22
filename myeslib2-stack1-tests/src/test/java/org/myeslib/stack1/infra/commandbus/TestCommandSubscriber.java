package org.myeslib.stack1.infra.commandbus;

import org.myeslib.infra.commandbus.CommandSubscriber;

public interface TestCommandSubscriber extends CommandSubscriber {

    public void on(TestCommand command);
}
