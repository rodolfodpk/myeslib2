package org.myeslib.sampledomain.aggregates.inventoryitem.events;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Event;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.domain.AutoValue_InventoryDecreased;

@AutoValue
public abstract class InventoryDecreased implements Event {
    public abstract Integer howMany();
    public static InventoryDecreased create(Integer howMany) {
        return new AutoValue_InventoryDecreased(howMany);
    }
}
