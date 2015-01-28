package org.myeslib.function;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkHistory;

import java.io.Serializable;
import java.util.List;

public interface SnapshotComputing<A extends AggregateRoot> extends Serializable {

    Snapshot<A> applyEventsOn(final A aggregateRootInstance, final UnitOfWorkHistory unitOfWorkHistory);

    Snapshot<A> applyEventsOn(final A aggregateRootInstance, final UnitOfWork unitOfWork);

    A applyEventsOn(A aggregateRootInstance, List<? extends Event> events);

    A applyEventsOn(final A aggregateRootInstance, final Event event);

}
