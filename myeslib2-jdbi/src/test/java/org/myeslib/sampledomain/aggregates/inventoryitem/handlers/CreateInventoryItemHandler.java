package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.CommandResults;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.jdbi.function.StatefulEventBus;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.services.SampleDomainService;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;


public class CreateInventoryItemHandler implements CommandHandler<CreateInventoryItem, InventoryItem> {

    final SampleDomainService service;

    public CreateInventoryItemHandler(SampleDomainService service) {
        checkNotNull(service);
        this.service = service;
    }

    @Override
    public CommandResults<UUID> handle(CreateInventoryItem command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        aggregateRoot.setService(service);
        final StatefulEventBus statefulBus = new StatefulEventBus(aggregateRoot);
        aggregateRoot.setInteractionContext(statefulBus);
        aggregateRoot.create(command.getTargetId());
        return new CommandResults<>(command, UnitOfWork.create(UUID.randomUUID(), command.getCommandId(), snapshot.getVersion(), statefulBus.getEvents()));
    }
}
