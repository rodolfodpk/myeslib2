package org.myeslib.jdbi.function.eventbus;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWorkHistory;
import org.myeslib.function.SnapshotComputing;

import java.util.List;

@SuppressWarnings("serial")
public class EventBusSnapshotComputing<A extends AggregateRoot> implements SnapshotComputing<A> {

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

    private void _applyEventsOn(final AggregateRoot instance, final List<? extends Event> events) {
        EventBusInteractionContext bus = new EventBusInteractionContext(instance);
        events.forEach(bus::apply);
    }
}
