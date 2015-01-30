package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.CommandResults;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.function.InteractionContext;
import org.myeslib.jdbi.function.multimethod.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;

import java.util.UUID;

public class DecreaseHandler implements CommandHandler<DecreaseInventory, InventoryItem> {

    public CommandResults<UUID> handle(DecreaseInventory command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);
        aggregateRoot.setInteractionContext(interactionContext);
        aggregateRoot.decrease(command.getHowMany());
        return new CommandResults<>(command, UnitOfWork.create(UUID.randomUUID(), command.getCommandId(), snapshot.getVersion(), interactionContext.getEvents()));
    }

}
