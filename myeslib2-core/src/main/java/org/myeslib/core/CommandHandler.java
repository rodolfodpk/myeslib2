package org.myeslib.core;

import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWork;

public interface CommandHandler<C extends Command, A extends AggregateRoot> {

    UnitOfWork handle(C command, Snapshot<A> snapshot);

}
