package sampledomain.aggregates.inventoryitem.handlers;

import net.jcip.annotations.NotThreadSafe;
import org.myeslib.core.CommandHandler;
import org.myeslib.core.StatefulCommandHandler;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.infra.InteractionContext;
import org.myeslib.infra.InteractionContextFactory;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import sampledomain.aggregates.inventoryitem.InventoryItem;
import sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

@NotThreadSafe
public class DecreaseHandler implements CommandHandler<DecreaseInventory>, StatefulCommandHandler {

    final InteractionContextFactory<InventoryItem> interactionContextFactory;
    final UnitOfWorkJournal<UUID> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;
    private Optional<UnitOfWork> unitOfWork = Optional.empty();

    @Inject
    public DecreaseHandler(InteractionContextFactory<InventoryItem> interactionContextFactory, UnitOfWorkJournal<UUID> journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.interactionContextFactory = interactionContextFactory;
        this.journal = journal;
        this.snapshotReader = snapshotReader;
    }

    public void handle(DecreaseInventory command) {
        final Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = interactionContextFactory.apply(aggregateRoot);
        aggregateRoot.setInteractionContext(interactionContext);
        aggregateRoot.decrease(command.howMany());
        this.unitOfWork = Optional.of(UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), snapshot.getVersion(), interactionContext.getAppliedEvents()));
        journal.append(command.targetId(), command.getCommandId(), command, unitOfWork.get());
    }

    @Override
    public Optional<UnitOfWork> getUnitOfWork() {
        return unitOfWork;
    }
}
