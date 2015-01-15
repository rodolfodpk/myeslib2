package org.myeslib.function;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Command;
import org.myeslib.data.CommandResults;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;

public interface CommandHandler<C extends Command, A extends AggregateRoot> {

    CommandResults handle(C command, Snapshot<A> snapshot);

}
