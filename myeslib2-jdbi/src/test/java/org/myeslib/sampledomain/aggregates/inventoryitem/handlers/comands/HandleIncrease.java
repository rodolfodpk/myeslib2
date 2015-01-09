package org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands;

import org.myeslib.core.CommandHandler;
import org.myeslib.core.Event;
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;

import java.util.Arrays;
import java.util.UUID;

public class HandleIncrease implements CommandHandler<IncreaseInventory, InventoryItem> {

    public UnitOfWork handle(IncreaseInventory command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final Event event = aggregateRoot.increase(command.getHowMany());
        return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
    }
}
