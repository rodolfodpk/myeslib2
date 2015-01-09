package org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands;

import org.myeslib.core.CommandHandler;
import org.myeslib.core.Event;
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.services.SampleDomainService;

import java.util.Arrays;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class HandleCreateThenIncreaseThenDecrease implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease, InventoryItem> {

    final SampleDomainService service;

    public HandleCreateThenIncreaseThenDecrease(SampleDomainService service) {
        checkNotNull(service);
        this.service = service;
    }

    @Override
    public UnitOfWork handle(CreateInventoryItemThenIncreaseThenDecrease command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        aggregateRoot.setService(service); // instead, it could be using Guice to inject necessary services
        final Event event1 = aggregateRoot.create(command.getId());
        final Event event2 = aggregateRoot.increase(command.getHowManyToIncrease());
        final Event event3 = aggregateRoot.decrease(command.getHowManyToDecrease());
        return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event1, event2, event3));
    }
}
