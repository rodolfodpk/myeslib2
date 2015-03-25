package sampledomain.aggregates.inventoryitem.commands;


import org.myeslib.data.Command;
import org.myeslib.stack1.infra.helpers.gson.polymorphic.RuntimeTypeAdapterFactory;

public class CommandsGsonAdapter {

    public static RuntimeTypeAdapterFactory<Command> commandAdapter() {

        final RuntimeTypeAdapterFactory<Command> commandAdapter =
                RuntimeTypeAdapterFactory.of(Command.class)
                        .registerSubtype(AutoValue_CreateInventoryItem.class, CreateInventoryItem.class.getSimpleName())
                        .registerSubtype(AutoValue_IncreaseInventory.class, IncreaseInventory.class.getSimpleName())
                        .registerSubtype(AutoValue_DecreaseInventory.class, DecreaseInventory.class.getSimpleName())
                        .registerSubtype(AutoValue_CreateInventoryItemThenIncreaseThenDecrease.class, CreateInventoryItemThenIncreaseThenDecrease.class.getSimpleName());

        return commandAdapter;

    }

}