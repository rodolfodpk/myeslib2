package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.CommandResults;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.jdbi.function.StatefulEventBus;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.services.SampleDomainService;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class CreateThenIncreaseThenDecreaseHandler implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease, InventoryItem> {

    final SampleDomainService service;

    public CreateThenIncreaseThenDecreaseHandler(SampleDomainService service) {
        checkNotNull(service);
        this.service = service;
    }

    @Override
    public CommandResults handle(CreateInventoryItemThenIncreaseThenDecrease command, Snapshot<InventoryItem> snapshot) {

        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final StatefulEventBus statefulBus = new StatefulEventBus(aggregateRoot);

        aggregateRoot.setService(service); // instead, it could be using Guice to inject necessary services
        aggregateRoot.setBus(statefulBus);

        aggregateRoot.create(command.getTargetId());
        aggregateRoot.increase(command.getHowManyToIncrease());
        aggregateRoot.decrease(command.getHowManyToDecrease());

        return new CommandResults(UnitOfWork.create(UUID.randomUUID(), command, snapshot.getVersion(), statefulBus.getEvents()));
    }
}
