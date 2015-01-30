package org.myeslib.sampledomain.aggregates.inventoryitem.handlers;

import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.CommandHandler;
import org.myeslib.function.InteractionContext;
import org.myeslib.jdbi.function.multimethod.MultiMethodInteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.services.SampleDomainService;

import javax.annotation.concurrent.Immutable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public class CreateThenIncreaseThenDecreaseHandler implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease, InventoryItem> {

    final SampleDomainService service;

    public CreateThenIncreaseThenDecreaseHandler(SampleDomainService service) {
        checkNotNull(service);
        this.service = service;
    }

    @Override
    public UnitOfWork handle(CreateInventoryItemThenIncreaseThenDecrease command, Snapshot<InventoryItem> snapshot) {

        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final InteractionContext interactionContext = new MultiMethodInteractionContext(aggregateRoot);

        aggregateRoot.setService(service); // instead, it could be using Guice to inject necessary services
        aggregateRoot.setInteractionContext(interactionContext);

        aggregateRoot.create(command.getTargetId());
        aggregateRoot.increase(command.getHowManyToIncrease());
        aggregateRoot.decrease(command.getHowManyToDecrease());

        return UnitOfWork.create(UUID.randomUUID(), command.getCommandId(), snapshot.getVersion(), interactionContext.getEvents());
    }
}
