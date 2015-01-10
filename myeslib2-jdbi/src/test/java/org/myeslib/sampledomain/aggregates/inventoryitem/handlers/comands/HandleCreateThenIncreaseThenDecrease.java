package org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands;

import org.myeslib.data.Snapshot;
import org.myeslib.function.CommandHandler;
import org.myeslib.core.Event;
import org.myeslib.data.UnitOfWork;

import org.myeslib.function.SnapshotComputing;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.services.SampleDomainService;

import java.util.Arrays;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class HandleCreateThenIncreaseThenDecrease implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease, InventoryItem> {

    final SampleDomainService service;
    final SnapshotComputing<InventoryItem> snapshotComputing;

    public HandleCreateThenIncreaseThenDecrease(SampleDomainService service, SnapshotComputing<InventoryItem> snapshotComputing) {
        checkNotNull(service);
        this.service = service;
        checkNotNull(snapshotComputing);
        this.snapshotComputing = snapshotComputing;
    }

    @Override
    public UnitOfWork handle(CreateInventoryItemThenIncreaseThenDecrease command, Snapshot<InventoryItem> snapshot) {

        final InventoryItem aggregateRoot1 = snapshot.getAggregateInstance();
        aggregateRoot1.setService(service); // instead, it could be using Guice to inject necessary services

        final Event event1 = aggregateRoot1.create(command.getId());
        final InventoryItem aggregateRoot2 = snapshotComputing.applyEventsOn(aggregateRoot1, event1);

        final Event event2 = aggregateRoot2.increase(command.getHowManyToIncrease());
        final InventoryItem aggregateRoot3 = snapshotComputing.applyEventsOn(aggregateRoot2, event2);

        final Event event3 = aggregateRoot3.decrease(command.getHowManyToDecrease());

        return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event1, event2, event3));
    }
}
