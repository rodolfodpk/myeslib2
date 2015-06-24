package sampledomain.aggregates.inventoryitem.events;

import org.immutables.value.Value;
import org.myeslib.data.Event;

import java.util.UUID;

@Value.Immutable
@Value.Style(strictBuilder = true)
public abstract class InventoryItemCreated implements Event {
    public abstract UUID id();
    public abstract String description();
    public static InventoryItemCreated create(UUID id, String description) {
        return ImmutableInventoryItemCreated.builder().id(id).description(description).build();
    }
}
