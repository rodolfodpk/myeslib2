package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.function.InteractionContext;
import org.myeslib.jdbi.function.multimethod.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.myeslib.storage.SnapshotReader;
import org.myeslib.storage.UnitOfWorkJournal;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;


public class CreateInventoryItemHandler implements CommandHandler<CreateInventoryItem, InventoryItem> {

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
    public void handle(CreateInventoryItem command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        aggregateRoot.setService(service);
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);
        aggregateRoot.setInteractionContext(interactionContext);
        aggregateRoot.create(command.targetId());
        UnitOfWork unitOfWork = UnitOfWork.create(UUID.randomUUID(), command.commandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());
        journal.append(command.targetId(), command.commandId(), command, unitOfWork);
    }
}
