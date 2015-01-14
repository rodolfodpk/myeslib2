package org.myeslib.sampledomain.aggregates.inventoryitem.events;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Event;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.domain.AutoValue_InventoryItemCreated;

import java.util.UUID;

@AutoValue
public abstract class InventoryItemCreated implements Event {
    public abstract UUID id();
    public abstract String description();
    InventoryItemCreated() {
    }
    public static InventoryItemCreated create(UUID id, String description) {
        return new AutoValue_InventoryItemCreated(id, description);
    }
}
