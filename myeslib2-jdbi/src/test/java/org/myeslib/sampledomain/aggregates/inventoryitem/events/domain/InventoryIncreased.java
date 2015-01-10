package org.myeslib.sampledomain.aggregates.inventoryitem.events.domain;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Event;

@AutoValue
public abstract class InventoryIncreased implements Event {
    public abstract Integer howMany();
    InventoryIncreased() {}
    public static InventoryIncreased create(Integer howMany) {
        return new AutoValue_InventoryIncreased(howMany);
    }
}
