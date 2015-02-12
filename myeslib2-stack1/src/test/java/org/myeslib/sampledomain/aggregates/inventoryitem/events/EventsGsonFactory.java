package org.myeslib.sampledomain.aggregates.inventoryitem.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.myeslib.data.Event;
import org.myeslib.stack1.infra.helpers.gson.polymorphic.RuntimeTypeAdapterFactory;
import org.myeslib.stack1.infra.helpers.gson.autovalue.AutoValueTypeAdapterFactory;

import java.lang.reflect.Modifier;

public class EventsGsonFactory {

    private final Gson gson;

    public EventsGsonFactory() {

        final RuntimeTypeAdapterFactory<Event> eventAdapter =
                RuntimeTypeAdapterFactory.of(Event.class)
                        .registerSubtype(AutoValue_InventoryItemCreated.class, InventoryItemCreated.class.getSimpleName())
                        .registerSubtype(AutoValue_InventoryIncreased.class, InventoryIncreased.class.getSimpleName())
                        .registerSubtype(AutoValue_InventoryDecreased.class, InventoryDecreased.class.getSimpleName());


        this.gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT)
                .registerTypeAdapterFactory(eventAdapter)
                .registerTypeAdapterFactory(new AutoValueTypeAdapterFactory())
                .setPrettyPrinting()
                .create();

    }

    public Gson create() {
        return gson;
    }

}
