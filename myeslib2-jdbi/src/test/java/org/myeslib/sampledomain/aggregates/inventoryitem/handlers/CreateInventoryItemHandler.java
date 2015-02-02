package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.core.CommandHandler;
import org.myeslib.jdbi.infra.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.infra.InteractionContext;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;


public class CreateInventoryItemHandler implements CommandHandler<CreateInventoryItem> {

    final SampleDomainService service;
    final UnitOfWorkJournal journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    public CreateInventoryItemHandler(SampleDomainService service, UnitOfWorkJournal journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.snapshotReader = snapshotReader;
        checkNotNull(service);
        this.service = service;
        checkNotNull(journal);
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
        final UnitOfWork unitOfWork = UnitOfWork.create(UUID.randomUUID(), command.commandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());
        journal.append(command.targetId(), command.commandId(), command, unitOfWork);
    }
}
