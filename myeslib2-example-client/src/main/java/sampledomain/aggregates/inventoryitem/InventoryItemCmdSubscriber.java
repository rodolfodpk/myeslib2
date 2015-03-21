package sampledomain.aggregates.inventoryitem;

import com.google.common.eventbus.Subscribe;
import org.myeslib.infra.failure.CommandErrorAware;
import sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import sampledomain.aggregates.inventoryitem.handlers.DecreaseHandler;
import sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;

import javax.inject.Inject;

public class InventoryItemCmdSubscriber {

    final CreateInventoryItemHandler createInventoryItemHandler;
    final IncreaseHandler increaseHandler;
    final DecreaseHandler decreaseHandler;
    final CreateThenIncreaseThenDecreaseHandler createThenIncreaseThenDecreaseHandler;

    @Inject
    public InventoryItemCmdSubscriber(CreateInventoryItemHandler createInventoryItemHandler, IncreaseHandler increaseHandler, DecreaseHandler decreaseHandler, CreateThenIncreaseThenDecreaseHandler createThenIncreaseThenDecreaseHandler) {
        this.createInventoryItemHandler = createInventoryItemHandler;
        this.increaseHandler = increaseHandler;
        this.decreaseHandler = decreaseHandler;
        this.createThenIncreaseThenDecreaseHandler = createThenIncreaseThenDecreaseHandler;
    }

    @Subscribe
    @CommandErrorAware
    public void on(CreateInventoryItem command) {
        createInventoryItemHandler.handle(command);
    }

    @Subscribe
    @CommandErrorAware
    public void on(IncreaseInventory command) {
        increaseHandler.handle(command);
    }

    @Subscribe
    @CommandErrorAware
    public void on(DecreaseInventory command) {
        decreaseHandler.handle(command);
    }

    @Subscribe
    @CommandErrorAware
    public void on(CreateInventoryItemThenIncreaseThenDecrease command) {
        createThenIncreaseThenDecreaseHandler.handle(command);
    }

}
