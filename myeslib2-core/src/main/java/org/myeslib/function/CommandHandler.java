package org.myeslib.function;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Command;
import org.myeslib.data.Snapshot;

public interface CommandHandler<C extends Command, A extends AggregateRoot> {

    void handle(C command, Snapshot<A> snapshot);

}
