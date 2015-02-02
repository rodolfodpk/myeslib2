package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.function.InteractionContext;
import org.myeslib.jdbi.function.multimethod.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.storage.SnapshotReader;
import org.myeslib.storage.UnitOfWorkJournal;

import java.util.UUID;

public class IncreaseHandler implements CommandHandler<IncreaseInventory, InventoryItem> {

    final UnitOfWorkJournal journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    public IncreaseHandler(UnitOfWorkJournal journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.journal = journal;
        this.snapshotReader = snapshotReader;
    }

    public void handle(IncreaseInventory command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);
        aggregateRoot.setInteractionContext(interactionContext);
        aggregateRoot.increase(command.howMany());
        UnitOfWork unitOfWork = UnitOfWork.create(UUID.randomUUID(), command.commandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());
        journal.append(command.targetId(), command.commandId(), command, unitOfWork);
    }
}
