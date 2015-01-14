package org.myeslib.sampledomain.aggregates.inventoryitem.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Command;
import org.myeslib.core.Event;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.jdbi.storage.helpers.gson.RuntimeTypeAdapterFactory;

import java.lang.reflect.Modifier;

public class SampleDomainGsonFactory {

    private final Gson gson;

    public SampleDomainGsonFactory() {

        final RuntimeTypeAdapterFactory<AggregateRoot> aggregateRootAdapter =
                RuntimeTypeAdapterFactory.of(AggregateRoot.class)
                        .registerSubtype(InventoryItem.class, InventoryItem.class.getSimpleName());

        final RuntimeTypeAdapterFactory<Command> commandAdapter =
                RuntimeTypeAdapterFactory.of(Command.class)
                        .registerSubtype(CreateInventoryItem.class, CreateInventoryItem.class.getSimpleName())
                        .registerSubtype(IncreaseInventory.class, IncreaseInventory.class.getSimpleName())
                        .registerSubtype(DecreaseInventory.class, DecreaseInventory.class.getSimpleName())
                        .registerSubtype(CreateInventoryItemThenIncreaseThenDecrease.class, CreateInventoryItemThenIncreaseThenDecrease.class.getSimpleName());

        final RuntimeTypeAdapterFactory<Event> eventAdapter =
                RuntimeTypeAdapterFactory.of(Event.class)
                        .registerSubtype(AutoValue_InventoryItemCreated.class, InventoryItemCreated.class.getSimpleName())
                        .registerSubtype(AutoValue_InventoryIncreased.class, InventoryIncreased.class.getSimpleName())
                        .registerSubtype(AutoValue_InventoryDecreased.class, InventoryDecreased.class.getSimpleName());


        this.gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT)
                .registerTypeAdapterFactory(aggregateRootAdapter)
                .registerTypeAdapterFactory(commandAdapter)
                .registerTypeAdapterFactory(eventAdapter)
                        //.setPrettyPrinting()
                .create();

    }

    public Gson create() {
        return gson;
    }

}
