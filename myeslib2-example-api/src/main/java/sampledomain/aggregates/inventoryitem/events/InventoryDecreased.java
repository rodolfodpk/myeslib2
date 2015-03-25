package sampledomain.aggregates.inventoryitem.events;

import com.google.auto.value.AutoValue;
import org.myeslib.data.Event;

@AutoValue
public abstract class InventoryDecreased implements Event {
    public abstract Integer howMany();
    public static InventoryDecreased create(Integer howMany) {
        return new AutoValue_InventoryDecreased(howMany);
    }
    //@AutoValue.Builder
    interface Builder {
        Builder howMany(Integer howMany);
        InventoryDecreased build();
    }
}
