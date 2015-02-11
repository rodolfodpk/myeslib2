package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import net.jcip.annotations.NotThreadSafe;
import org.myeslib.core.CommandHandler;
import org.myeslib.core.StatefulCommandHandler;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.infra.InteractionContext;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.stack1.infra.MultiMethodInteractionContext;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@NotThreadSafe
public class DecreaseHandler implements CommandHandler<DecreaseInventory>, StatefulCommandHandler {

    final UnitOfWorkJournal<UUID> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;
    final Supplier<UnitOfWorkId> uowIdSupplier;
    private Optional<UnitOfWork> unitOfWork = Optional.empty();

    @Inject
    public DecreaseHandler(UnitOfWorkJournal<UUID> journal, SnapshotReader<UUID, InventoryItem> snapshotReader, Supplier<UnitOfWorkId> uowIdSupplier) {
        this.journal = journal;
        this.snapshotReader = snapshotReader;
        this.uowIdSupplier = uowIdSupplier;
    }

    public void handle(DecreaseInventory command) {
        final Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);
        aggregateRoot.setInteractionContext(interactionContext);
        aggregateRoot.decrease(command.howMany());
        this.unitOfWork = Optional.of(UnitOfWork.create(uowIdSupplier.get(), command.getCommandId(), snapshot.getVersion(), interactionContext.getAppliedEvents()));
        journal.append(command.targetId(), command.getCommandId(), command, unitOfWork.get());
    }

    @Override
    public Optional<UnitOfWork> getUnitOfWork() {
        return unitOfWork;
    }
}
