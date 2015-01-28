package org.myeslib.jdbi.function;

import autovalue.shaded.com.google.common.common.collect.ImmutableList;
import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkHistory;
import org.myeslib.function.SnapshotComputing;

import java.util.List;

@SuppressWarnings("serial")
public class MutableSnapshotComputing2<A extends AggregateRoot> implements SnapshotComputing<A> {

    @Override
    public Snapshot<A> applyEventsOn(final A aggregateRootInstance,
                                     final UnitOfWorkHistory transactionHistory) {
        _applyEventsOn(aggregateRootInstance, transactionHistory.getAllEvents());
        return new Snapshot<>(aggregateRootInstance, transactionHistory.getLastVersion());
    }

    @Override
    public Snapshot<A> applyEventsOn(final A aggregateRootInstance,
                                     final UnitOfWork uow) {
        _applyEventsOn(aggregateRootInstance, uow.getEvents());
        return new Snapshot<>(aggregateRootInstance, uow.getVersion());
    }

    @Override
    public A applyEventsOn(A aggregateRootInstance, List<? extends Event> events) {
        _applyEventsOn(aggregateRootInstance, events);
        return aggregateRootInstance;
    }

    @Override
    public A applyEventsOn(final A aggregateRootInstance, final Event event) {
        _applyEventsOn(aggregateRootInstance, ImmutableList.of(event));
        return aggregateRootInstance;
    }

    private void _applyEventsOn(final AggregateRoot instance, final List<? extends Event> events) {
        StatefulEventBus bus = new StatefulEventBus(instance);
        events.forEach(bus::apply);
    }
}
