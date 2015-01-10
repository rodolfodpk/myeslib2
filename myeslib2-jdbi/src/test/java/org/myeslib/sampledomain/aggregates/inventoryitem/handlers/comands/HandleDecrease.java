package org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands;

import org.myeslib.core.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;

import java.util.Arrays;
import java.util.UUID;

public class HandleDecrease implements CommandHandler<DecreaseInventory, InventoryItem> {

    public UnitOfWork handle(DecreaseInventory command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final Event event = aggregateRoot.decrease(command.getHowMany());
        return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
    }

}
