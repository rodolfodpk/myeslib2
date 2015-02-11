package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import net.jcip.annotations.ThreadSafe;
import org.myeslib.core.CommandHandler;
import org.myeslib.stack1.data.Stack1UnitOfWorkId;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.stack1.data.Stack1UnitOfWork;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.stack1.infra.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.myeslib.infra.InteractionContext;

import javax.inject.Inject;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@ThreadSafe
public class CreateThenIncreaseThenDecreaseHandler implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease> {

    final SampleDomainService service;
    final UnitOfWorkJournal<UUID> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;

    @Inject
    public CreateThenIncreaseThenDecreaseHandler(SampleDomainService service, UnitOfWorkJournal<UUID> journal, SnapshotReader<UUID, InventoryItem> snapshotReader) {
        this.snapshotReader = snapshotReader;
        this.journal = journal;
        this.service = service;
    }

    @Override
    public void handle(CreateInventoryItemThenIncreaseThenDecrease command) {

        final Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.targetId());
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);

        aggregateRoot.setService(service); // if aggregateRoot uses many domain services, it could be using Guice to inject necessary services
        aggregateRoot.setInteractionContext(interactionContext);

        aggregateRoot.create(command.targetId());
        aggregateRoot.increase(command.howManyToIncrease());
        aggregateRoot.decrease(command.howManyToDecrease());

        final UnitOfWork UnitOfWork = Stack1UnitOfWork.create(Stack1UnitOfWorkId.create(), command.getCommandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());

        journal.append(command.targetId(), command.getCommandId(), command, UnitOfWork);
    }
}
