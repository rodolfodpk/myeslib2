package org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands;

import com.google.common.eventbus.EventBus;
import org.myeslib.data.Snapshot;
import org.myeslib.function.CommandHandler;
import org.myeslib.core.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.jdbi.function.StatefulEventBus;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;

import java.util.Arrays;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class HandleIncrease implements CommandHandler<IncreaseInventory, InventoryItem> {

    final EventBus bus;

    public HandleIncrease(EventBus bus) {
        checkNotNull(bus);
        this.bus = bus;
    }

    public UnitOfWork handle(IncreaseInventory command, Snapshot<InventoryItem> snapshot) {
        final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
        final StatefulEventBus statefulBus = new StatefulEventBus(aggregateRoot, bus);
        aggregateRoot.setBus(statefulBus);
        aggregateRoot.increase(command.getHowMany());
        return UnitOfWork.create(UUID.randomUUID(), command, statefulBus.getEvents());
    }
}
