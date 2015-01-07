package org.myeslib.storage.helpers.eventsource;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.core.data.UnitOfWorkHistory;

import java.util.List;

public class SnapshotHelper<A extends AggregateRoot> {

    public Snapshot<A> applyEventsOn(final A aggregateRootInstance,
                                     final UnitOfWorkHistory transactionHistory
                                     ) {
        applyEventsOn(transactionHistory.getAllEvents(), aggregateRootInstance);
        return new Snapshot<>(aggregateRootInstance, transactionHistory.getLastVersion());
    }

    public Snapshot<A> applyEventsOn(final A aggregateRootInstance,
                                     final UnitOfWork uow
                                     ) {
        applyEventsOn(uow.getEvents(), aggregateRootInstance);
        return new Snapshot<>(aggregateRootInstance, uow.getVersion());
    }

    private void applyEventsOn(List<? extends Event> events, AggregateRoot instance) {
        MultiMethod mm = MultiMethod.getMultiMethod(instance.getClass(), "on");
        for (Event event : events) {
            try {
                mm.invoke(instance, event);
            } catch (Exception e) {
                throw new RuntimeException("Error when executing with reflection");
            }
        }
    }
}
