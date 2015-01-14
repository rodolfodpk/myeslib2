package org.myeslib.sampledomain.aggregates.inventoryitem.events;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Event;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.domain.AutoValue_InventoryIncreased;

@AutoValue
public abstract class InventoryIncreased implements Event {
    public abstract Integer howMany();
    InventoryIncreased() {}
    public static InventoryIncreased create(Integer howMany) {
        return new AutoValue_InventoryIncreased(howMany);
    }
}
