package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import net.jcip.annotations.ThreadSafe;
import org.myeslib.core.CommandHandler;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.infra.InteractionContext;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.stack1.infra.MultiMethodInteractionContext;

import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Supplier;

@ThreadSafe
public class IncreaseHandler implements CommandHandler<IncreaseInventory> {

    final UnitOfWorkJournal<UUID> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;
    final Supplier<UnitOfWorkId> uowIdSupplier;

    @Inject
    public IncreaseHandler(UnitOfWorkJournal<UUID> journal, SnapshotReader<UUID, InventoryItem> snapshotReader, Supplier<UnitOfWorkId> uowIdSupplier) {
        this.journal = journal;
        this.snapshotReader = snapshotReader;
        this.uowIdSupplier = uowIdSupplier;
    }

    public void handle(IncreaseInventory command) {
        final Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);
        aggregateRoot.setInteractionContext(interactionContext);
        aggregateRoot.increase(command.howMany());
        final UnitOfWork unitOfWork = UnitOfWork.create(uowIdSupplier.get(), command.getCommandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());
        journal.append(command.targetId(), command.getCommandId(), command, unitOfWork);
    }

}
