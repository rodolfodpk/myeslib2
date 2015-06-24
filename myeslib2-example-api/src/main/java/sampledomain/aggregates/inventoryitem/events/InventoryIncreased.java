package sampledomain.aggregates.inventoryitem.events;

import org.immutables.value.Value;
import org.myeslib.data.Event;

@Value.Immutable
@Value.Style(strictBuilder = true)
public abstract class InventoryIncreased implements Event {
    public abstract Integer howMany();
    public static InventoryIncreased create(Integer howMany) {
        return ImmutableInventoryIncreased.builder().howMany(howMany).build();
    }
}
