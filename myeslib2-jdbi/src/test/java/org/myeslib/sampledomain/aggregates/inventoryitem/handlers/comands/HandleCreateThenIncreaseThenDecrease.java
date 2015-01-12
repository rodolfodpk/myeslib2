package org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands;

import com.google.common.eventbus.EventBus;
import org.myeslib.core.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.function.SnapshotComputing;
import org.myeslib.jdbi.function.StatefulEventBus;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.services.SampleDomainService;

import java.util.Arrays;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class HandleCreateThenIncreaseThenDecrease implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease, InventoryItem> {

    final SampleDomainService service;
    final EventBus bus;

    public HandleCreateThenIncreaseThenDecrease(SampleDomainService service, EventBus bus) {
        checkNotNull(service);
        this.service = service;
        checkNotNull(bus);
        this.bus = bus;
    }

    @Override
    public UnitOfWork handle(CreateInventoryItemThenIncreaseThenDecrease command, Snapshot<InventoryItem> snapshot) {

        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final StatefulEventBus statefulBus = new StatefulEventBus(aggregateRoot, bus);

        aggregateRoot.setService(service); // instead, it could be using Guice to inject necessary services
        aggregateRoot.setBus(statefulBus);

        aggregateRoot.create(command.getId());
        aggregateRoot.increase(command.getHowManyToIncrease());
        aggregateRoot.decrease(command.getHowManyToDecrease());

        return UnitOfWork.create(UUID.randomUUID(), command, statefulBus.getEvents());
    }
}
