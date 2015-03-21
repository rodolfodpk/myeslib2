package sampledomain.aggregates.inventoryitem;

import org.myeslib.infra.commandbus.CommandSubscriber;
import sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import sampledomain.aggregates.inventoryitem.handlers.DecreaseHandler;
import sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;

import javax.inject.Inject;

public class InventoryItemCmdSubscriber implements CommandSubscriber {

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

    public void on(CreateInventoryItem command) {
        createInventoryItemHandler.handle(command);
    }

    public void on(IncreaseInventory command) {
        increaseHandler.handle(command);
    }

    public void on(DecreaseInventory command) {
        decreaseHandler.handle(command);
    }

    public void on(CreateInventoryItemThenIncreaseThenDecrease command) {
        createThenIncreaseThenDecreaseHandler.handle(command);
    }

}
