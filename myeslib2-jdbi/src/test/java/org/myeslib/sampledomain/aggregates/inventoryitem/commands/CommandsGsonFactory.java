package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.myeslib.core.Command;
import org.myeslib.jdbi.storage.helpers.gson.RuntimeTypeAdapterFactory;

import java.lang.reflect.Modifier;

public class CommandsGsonFactory {

    private final Gson gson;

    public CommandsGsonFactory() {

        final RuntimeTypeAdapterFactory<Command> commandAdapter =
                RuntimeTypeAdapterFactory.of(Command.class)
                        .registerSubtype(AutoValue_CreateInventoryItem.class, CreateInventoryItem.class.getSimpleName())
                        .registerSubtype(AutoValue_IncreaseInventory.class, IncreaseInventory.class.getSimpleName())
                        .registerSubtype(AutoValue_DecreaseInventory.class, DecreaseInventory.class.getSimpleName())
                        .registerSubtype(AutoValue_CreateInventoryItemThenIncreaseThenDecrease.class, CreateInventoryItemThenIncreaseThenDecrease.class.getSimpleName());

        this.gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT)
                .registerTypeAdapterFactory(commandAdapter)
                .setPrettyPrinting()
                .create();

    }

    public Gson create() {
        return gson;
    }

}
