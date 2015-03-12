package org.myeslib.sampledomain.aggregates.inventoryitem;

import com.google.common.eventbus.Subscribe;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.DecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;

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
    public void on(CreateInventoryItem command) {
        createInventoryItemHandler.handle(command);
    }

    @Subscribe
    public void on(IncreaseInventory command) {
        increaseHandler.handle(command);
    }

    @Subscribe
    public void on(DecreaseInventory command) {
        decreaseHandler.handle(command);
    }

    @Subscribe
    public void on(CreateInventoryItemThenIncreaseThenDecrease command) {
        createThenIncreaseThenDecreaseHandler.handle(command);
    }
}
