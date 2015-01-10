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
public class SnapshotComputingImpl<A extends AggregateRoot> implements SnapshotComputing<A> {

    @Override
    public Snapshot<A> applyEventsOn(final A aggregateRootInstance,
                                     final UnitOfWorkHistory transactionHistory) {
        applyEventsOn(aggregateRootInstance, transactionHistory.getAllEvents());
        return new Snapshot<>(aggregateRootInstance, transactionHistory.getLastVersion());
    }

    @Override
    public Snapshot<A> applyEventsOn(final A aggregateRootInstance,
                                     final UnitOfWork uow) {
        applyEventsOn(aggregateRootInstance, uow.getEvents());
        return new Snapshot<>(aggregateRootInstance, uow.getVersion());
    }

    @Override
    public A applyEventsOn(A aggregateRootInstance, Event event) {
        applyEventsOn(aggregateRootInstance, ImmutableList.of(event));
        return aggregateRootInstance;
    }

    private void applyEventsOn(AggregateRoot instance, List<? extends Event> events) {
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
