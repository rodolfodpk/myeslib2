package org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands;

import org.myeslib.core.CommandHandler;
import org.myeslib.core.Event;
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.services.SampleDomainService;


import java.util.Arrays;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;


public class HandleCreateInventoryItem implements CommandHandler<CreateInventoryItem, InventoryItem> {

    final SampleDomainService service;

    public HandleCreateInventoryItem(SampleDomainService service) {
        checkNotNull(service);
        this.service = service;
    }

    @Override
    public UnitOfWork handle(CreateInventoryItem command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        aggregateRoot.setService(service); // instead, it could be using Guice to inject necessary services
        final Event event = aggregateRoot.create(command.getId());
        return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
    }
}
