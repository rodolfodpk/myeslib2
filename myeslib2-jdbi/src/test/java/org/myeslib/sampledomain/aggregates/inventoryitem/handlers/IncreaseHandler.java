package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.core.CommandHandler;
import org.myeslib.jdbi.infra.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.infra.InteractionContext;

import javax.inject.Inject;
import java.util.UUID;

public class IncreaseHandler implements CommandHandler<IncreaseInventory> {

    final UnitOfWorkJournal<UUID> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    @Inject
    public IncreaseHandler(UnitOfWorkJournal<UUID> journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.journal = journal;
        this.snapshotReader = snapshotReader;
    }

    public void handle(IncreaseInventory command) {
        final Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);
        aggregateRoot.setInteractionContext(interactionContext);
        aggregateRoot.increase(command.howMany());
        final UnitOfWork unitOfWork = UnitOfWork.create(UUID.randomUUID(), command.commandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());
        journal.append(command.targetId(), command.commandId(), command, unitOfWork);
    }

}
