package sampledomain.aggregates.inventoryitem.events;

import com.google.auto.value.AutoValue;
import org.myeslib.data.Event;

@AutoValue
public abstract class InventoryIncreased implements Event {
    InventoryIncreased() {}
    public abstract Integer howMany();
    public static InventoryIncreased create(Integer howMany) {
        return new AutoValue_InventoryIncreased(howMany);
    }
}
