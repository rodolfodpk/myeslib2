package org.myeslib.sampledomain.aggregates.inventoryitem.events.domain;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Event;

@AutoValue
public abstract class InventoryDecreased implements Event {
    public abstract Integer howMany();
    public static InventoryDecreased create(Integer howMany) {
        return new AutoValue_InventoryDecreased(howMany);
    }
}
