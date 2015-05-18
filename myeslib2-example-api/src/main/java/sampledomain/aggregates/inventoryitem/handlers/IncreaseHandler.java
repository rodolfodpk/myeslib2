package sampledomain.aggregates.inventoryitem.handlers;

import net.jcip.annotations.ThreadSafe;
import org.myeslib.core.CommandHandler;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.WriteModelJournal;
import sampledomain.aggregates.inventoryitem.InventoryItem;
import sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;

import javax.inject.Inject;
import java.util.UUID;

@ThreadSafe
public class IncreaseHandler implements CommandHandler<IncreaseInventory> {

    final WriteModelJournal<UUID, InventoryItem> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    @Inject
    public IncreaseHandler(WriteModelJournal<UUID, InventoryItem> journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.journal = journal;
        this.snapshotReader = snapshotReader;
    }

    public void handle(IncreaseInventory command) {
        final Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();

        aggregateRoot.increase(command.howMany());

        final UnitOfWork unitOfWork = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), snapshot.getVersion(), aggregateRoot.getEmittedEvents());
        journal.append(command.targetId(), command, unitOfWork);
    }

}
