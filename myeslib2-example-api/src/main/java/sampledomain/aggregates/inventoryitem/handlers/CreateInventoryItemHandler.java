package sampledomain.aggregates.inventoryitem.handlers;

import net.jcip.annotations.ThreadSafe;
import org.myeslib.core.CommandHandler;
import org.myeslib.infra.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.WriteModelJournal;
import sampledomain.aggregates.inventoryitem.InventoryItem;
import sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;

import javax.inject.Inject;
import java.util.UUID;

@ThreadSafe
public class CreateInventoryItemHandler implements CommandHandler<CreateInventoryItem> {

    final WriteModelJournal<UUID> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    @Inject
    public CreateInventoryItemHandler(WriteModelJournal<UUID> journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.snapshotReader = snapshotReader;
        this.journal = journal;
    }

    @Override
    public void handle(CreateInventoryItem command) {
        final Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();

        aggregateRoot.create(command.targetId());

        final UnitOfWork unitOfWork = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), snapshot.getVersion(), aggregateRoot.getEmittedEvents());
        journal.append(command.targetId(), command.getCommandId(), command, unitOfWork);
    }
}
