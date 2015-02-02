package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.core.CommandHandler;
import org.myeslib.jdbi.infra.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.infra.InteractionContext;

import java.util.UUID;

public class DecreaseHandler implements CommandHandler<DecreaseInventory> {

    final UnitOfWorkJournal journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    public DecreaseHandler(UnitOfWorkJournal journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.journal = journal;
        this.snapshotReader = snapshotReader;
    }

    public void handle(DecreaseInventory command) {
        final Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);
        aggregateRoot.setInteractionContext(interactionContext);
        aggregateRoot.decrease(command.howMany());
        final UnitOfWork unitOfWork = UnitOfWork.create(UUID.randomUUID(), command.commandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());
        journal.append(command.targetId(), command.commandId(), command, unitOfWork);
    }
}
