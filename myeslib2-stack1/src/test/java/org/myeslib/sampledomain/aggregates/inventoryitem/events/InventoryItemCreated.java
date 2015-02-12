package org.myeslib.sampledomain.aggregates.inventoryitem.events;

import com.google.auto.value.AutoValue;
import org.myeslib.data.Event;

import java.util.UUID;

@AutoValue
public abstract class InventoryItemCreated implements Event {
    InventoryItemCreated() {}
    public abstract UUID id();
    public abstract String description();
    public static InventoryItemCreated create(UUID id, String description) {
        return new AutoValue_InventoryItemCreated(id, description);
    }
}
