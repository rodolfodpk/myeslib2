package org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands;

import org.myeslib.core.CommandHandler;
import org.myeslib.core.Event;
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;

import java.util.Arrays;
import java.util.UUID;

public class HandleDecrease implements CommandHandler<DecreaseInventory, InventoryItem> {

    public UnitOfWork handle(DecreaseInventory command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final Event event = aggregateRoot.decrease(command.getHowMany());
        return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
    }

}
