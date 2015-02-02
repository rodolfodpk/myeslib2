package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.function.InteractionContext;
import org.myeslib.jdbi.function.multimethod.MultiMethodInteractionContext;
import org.myeslib.jdbi.storage.JdbiJournal;
import org.myeslib.jdbi.storage.dao.JdbiDao;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.services.SampleDomainService;

import javax.annotation.concurrent.Immutable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public class CreateThenIncreaseThenDecreaseHandler implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease, InventoryItem> {

    final SampleDomainService service;
    final JdbiJournal<UUID> journal;

    public CreateThenIncreaseThenDecreaseHandler(SampleDomainService service, JdbiJournal<UUID> journal) {
        checkNotNull(journal);
        this.journal = journal;
        checkNotNull(service);
        this.service = service;
    }

    @Override
    public void handle(CreateInventoryItemThenIncreaseThenDecrease command, Snapshot<InventoryItem> snapshot) {

        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);

        aggregateRoot.setService(service); // instead, it could be using Guice to inject necessary services
        aggregateRoot.setInteractionContext(interactionContext);

        aggregateRoot.create(command.targetId());
        aggregateRoot.increase(command.howManyToIncrease());
        aggregateRoot.decrease(command.howManyToDecrease());

        UnitOfWork unitOfWork = UnitOfWork.create(UUID.randomUUID(), command.commandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());

        journal.append(command.targetId(), command.commandId(), command, unitOfWork);
    }
}
