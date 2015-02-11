package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import net.jcip.annotations.ThreadSafe;
import org.myeslib.stack1.data.Stack1UnitOfWorkId;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.stack1.data.Stack1UnitOfWork;
import org.myeslib.core.CommandHandler;
import org.myeslib.stack1.infra.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.infra.InteractionContext;

import javax.inject.Inject;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@ThreadSafe
public class CreateInventoryItemHandler implements CommandHandler<CreateInventoryItem> {

    final SampleDomainService service;
    final UnitOfWorkJournal<UUID> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    @Inject
    public CreateInventoryItemHandler(SampleDomainService service, UnitOfWorkJournal<UUID> journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.snapshotReader = snapshotReader;
        this.service = service;
        this.journal = journal;
    }

    @Override
    public void handle(CreateInventoryItem command) {
        final Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        aggregateRoot.setService(service);
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);
        aggregateRoot.setInteractionContext(interactionContext);
        aggregateRoot.create(command.targetId());
        final UnitOfWork UnitOfWork = Stack1UnitOfWork.create(Stack1UnitOfWorkId.create(), command.getCommandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());
        journal.append(command.targetId(), command.getCommandId(), command, UnitOfWork);
    }
}
