package sampledomain.aggregates.inventoryitem.handlers;

import net.jcip.annotations.ThreadSafe;
import org.myeslib.core.CommandHandler;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.infra.InteractionContext;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import sampledomain.aggregates.inventoryitem.InventoryItem;
import sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import sampledomain.services.SampleDomainService;
import org.myeslib.stack1.infra.MultiMethodInteractionContext;

import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Supplier;

@ThreadSafe
public class CreateThenIncreaseThenDecreaseHandler implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease> {

    final SampleDomainService service;
    final UnitOfWorkJournal<UUID> journal;
    final SnapshotReader<UUID, InventoryItem> snapshotReader;
    final Supplier<UnitOfWorkId> uowIdSupplier;

    @Inject
    public CreateThenIncreaseThenDecreaseHandler(SampleDomainService service, UnitOfWorkJournal<UUID> journal, SnapshotReader<UUID, InventoryItem> snapshotReader, Supplier<UnitOfWorkId> uowIdSupplier) {
        this.snapshotReader = snapshotReader;
        this.journal = journal;
        this.service = service;
        this.uowIdSupplier = uowIdSupplier;
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

        final UnitOfWork unitOfWork = UnitOfWork.create(uowIdSupplier.get(), command.getCommandId(), snapshot.getVersion(), interactionContext.getAppliedEvents());

        journal.append(command.targetId(), command.getCommandId(), command, unitOfWork);
    }
}
