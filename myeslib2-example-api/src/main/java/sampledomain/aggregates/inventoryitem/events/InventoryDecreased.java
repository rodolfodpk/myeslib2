package sampledomain.aggregates.inventoryitem.events;

import org.immutables.value.Value;
import org.myeslib.data.Event;

@Value.Immutable
@Value.Style(strictBuilder = true)
public abstract class InventoryDecreased implements Event {
    public abstract Integer howMany();
    public static InventoryDecreased create(Integer howMany) {
        return ImmutableInventoryDecreased.builder().howMany(howMany).build();
    }
}
