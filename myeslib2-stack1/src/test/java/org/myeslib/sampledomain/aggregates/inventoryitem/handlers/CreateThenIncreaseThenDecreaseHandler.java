package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import net.jcip.annotations.ThreadSafe;
import org.myeslib.core.CommandHandler;
import org.myeslib.infra.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.infra.InteractionContext;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.WriteModelJournal;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.myeslib.stack1.infra.Stack1InteractionContext;

import javax.inject.Inject;
import java.util.UUID;

@ThreadSafe
public class CreateThenIncreaseThenDecreaseHandler implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease> {

    final SampleDomainService service;
    final WriteModelJournal<UUID> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    @Inject
    public CreateThenIncreaseThenDecreaseHandler(SampleDomainService service, WriteModelJournal<UUID> journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.snapshotReader = snapshotReader;
        this.journal = journal;
        this.service = service;
    }

    @Override
    public void handle(CreateInventoryItemThenIncreaseThenDecrease command) {

        final Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new Stack1InteractionContext(aggregateRoot);

        aggregateRoot.setService(service); // if aggregateRoot uses many domain services, it could be using Guice to inject necessary services
        aggregateRoot.setInteractionContext(interactionContext);

        aggregateRoot.create(command.targetId());
        aggregateRoot.increase(command.howManyToIncrease());
        aggregateRoot.decrease(command.howManyToDecrease());

        final UnitOfWork unitOfWork = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), snapshot.getVersion(), interactionContext.getEmittedEvents());

        journal.append(command.targetId(), command.getCommandId(), command, unitOfWork);
    }
}
