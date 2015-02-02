package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.core.CommandHandler;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.jdbi.infra.JdbiJournal;
import org.myeslib.jdbi.infra.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.myeslib.infra.InteractionContext;

import javax.annotation.concurrent.Immutable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public class CreateThenIncreaseThenDecreaseHandler implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease> {

    final SampleDomainService service;
    final JdbiJournal<UUID> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    public CreateThenIncreaseThenDecreaseHandler(SampleDomainService service, JdbiJournal<UUID> journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        checkNotNull(snapshotReader);
        this.snapshotReader = snapshotReader;
        checkNotNull(journal);
        this.journal = journal;
        checkNotNull(service);
        this.service = service;
    }

    @Override
    public void handle(CreateInventoryItemThenIncreaseThenDecrease command) {

        Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);

        aggregateRoot.setService(service); // instead, it could be using Guice to inject necessary services
        aggregateRoot.setInteractionContext(interactionContext);

        aggregateRoot.create(command.targetId());
        aggregateRoot.increase(command.howManyToIncrease());
        aggregateRoot.decrease(command.howManyToDecrease());

        final UnitOfWork unitOfWork = UnitOfWork.create(UUID.randomUUID(), command.commandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());

        journal.append(command.targetId(), command.commandId(), command, unitOfWork);
    }
}
