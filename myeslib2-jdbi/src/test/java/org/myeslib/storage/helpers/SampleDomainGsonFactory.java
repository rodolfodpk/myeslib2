package org.myeslib.storage.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Command;
import org.myeslib.core.Event;
import org.myeslib.storage.helpers.gson.RuntimeTypeAdapterFactory;

import java.lang.reflect.Modifier;

import static org.myeslib.storage.helpers.SampleDomain.* ;

/*
 * Produces a Gson instance able to ser/deserialize polymorfic types. 
 * Jackson is probably faster but it requires those @JsonCreator and @JsonProperty annotations which can be a bit verbose and error prone.
 */
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
                        .registerSubtype(InventoryItemCreated.class, InventoryItemCreated.class.getSimpleName())
                        .registerSubtype(InventoryIncreased.class, InventoryIncreased.class.getSimpleName())
                        .registerSubtype(InventoryDecreased.class, InventoryDecreased.class.getSimpleName());


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
