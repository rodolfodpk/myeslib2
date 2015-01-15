package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.CommandResults;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.jdbi.function.StatefulEventBus;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;

import java.util.UUID;

public class IncreaseHandler implements CommandHandler<IncreaseInventory, InventoryItem> {

    public CommandResults handle(IncreaseInventory command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final StatefulEventBus statefulBus = new StatefulEventBus(aggregateRoot);
        aggregateRoot.setBus(statefulBus);
        aggregateRoot.increase(command.getHowMany());
        return new CommandResults(UnitOfWork.create(UUID.randomUUID(), command, statefulBus.getEvents()));
    }
}
