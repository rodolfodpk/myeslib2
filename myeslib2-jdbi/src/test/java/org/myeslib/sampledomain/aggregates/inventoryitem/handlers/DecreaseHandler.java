package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.CommandResults;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.jdbi.function.StatefulEventBus;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;

import java.util.UUID;

public class DecreaseHandler implements CommandHandler<DecreaseInventory, InventoryItem> {

    public CommandResults handle(DecreaseInventory command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final StatefulEventBus statefulBus = new StatefulEventBus(aggregateRoot);
        aggregateRoot.setBus(statefulBus);
        aggregateRoot.decrease(command.getHowMany());
        return new CommandResults(UnitOfWork.create(UUID.randomUUID(), command, snapshot.getVersion(), statefulBus.getEvents()));
    }

}
