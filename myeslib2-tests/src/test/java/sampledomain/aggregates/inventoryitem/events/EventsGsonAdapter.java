package sampledomain.aggregates.inventoryitem.events;

import org.myeslib.data.Event;
import org.myeslib.stack1.infra.helpers.gson.polymorphic.RuntimeTypeAdapterFactory;

public class EventsGsonAdapter {

    public static RuntimeTypeAdapterFactory<Event> eventAdapter() {

        final RuntimeTypeAdapterFactory<Event> eventAdapter =
                RuntimeTypeAdapterFactory.of(Event.class)
                        .registerSubtype(AutoValue_InventoryItemCreated.class, InventoryItemCreated.class.getSimpleName())
                        .registerSubtype(AutoValue_InventoryIncreased.class, InventoryIncreased.class.getSimpleName())
                        .registerSubtype(AutoValue_InventoryDecreased.class, InventoryDecreased.class.getSimpleName());

        return eventAdapter;

    }

}
