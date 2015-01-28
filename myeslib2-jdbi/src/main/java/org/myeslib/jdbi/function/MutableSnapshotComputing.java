package org.myeslib.jdbi.function;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWorkHistory;
import org.myeslib.function.SnapshotComputing;

import java.util.List;

@SuppressWarnings("serial")
public class MutableSnapshotComputing<A extends AggregateRoot> implements SnapshotComputing<A> {

    @Override
    public Snapshot<A> applyEventsOn(final A aggregateRootInstance,
                                     final UnitOfWorkHistory transactionHistory) {
        _applyEventsOn(aggregateRootInstance, transactionHistory.getAllEvents());
        return new Snapshot<>(aggregateRootInstance, transactionHistory.getLastVersion());
    }

    @Override
    public A applyEventsOn(A aggregateRootInstance, List<? extends Event> events) {
        _applyEventsOn(aggregateRootInstance, events);
        return aggregateRootInstance;
    }

    private void _applyEventsOn(AggregateRoot instance, List<? extends Event> events) {
        MultiMethod mm = MultiMethod.getMultiMethod(instance.getClass(), "on");
        for (Event event : events) {
            try {
                mm.invoke(instance, event);
            } catch (Exception e) {
                throw new RuntimeException("Error when executing with reflection", e.getCause());
            }
        }
    }
}
