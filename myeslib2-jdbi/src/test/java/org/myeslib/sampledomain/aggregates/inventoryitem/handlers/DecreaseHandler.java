package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.function.InteractionContext;
import org.myeslib.jdbi.function.multimethod.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.storage.SnapshotReader;
import org.myeslib.storage.UnitOfWorkJournal;

import java.util.UUID;

public class DecreaseHandler implements CommandHandler<DecreaseInventory, InventoryItem> {

    final UnitOfWorkJournal journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    public DecreaseHandler(UnitOfWorkJournal journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.journal = journal;
        this.snapshotReader = snapshotReader;
    }

    public void handle(DecreaseInventory command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);
        aggregateRoot.setInteractionContext(interactionContext);
        aggregateRoot.decrease(command.howMany());
        UnitOfWork unitOfWork = UnitOfWork.create(UUID.randomUUID(), command.commandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());
        journal.append(command.targetId(), command.commandId(), command, unitOfWork);
    }
}
