package sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;

import java.util.UUID;

@AutoValue
public abstract class CreateInventoryItem implements Command {
    public abstract CommandId getCommandId();
    public abstract UUID targetId();
    public static CreateInventoryItem create(CommandId commandId, UUID targetId) {
        return new AutoValue_CreateInventoryItem(commandId, targetId);
    }
}
