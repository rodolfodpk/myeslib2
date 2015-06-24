package sampledomain.aggregates.inventoryitem.commands;

import org.immutables.value.Value;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;

import java.util.UUID;

@Value.Immutable
@Value.Style(strictBuilder = true)
public abstract class CreateInventoryItem implements Command {
    public abstract UUID targetId();
    public static CreateInventoryItem create(CommandId commandId, UUID targetId) {
        return ImmutableCreateInventoryItem.builder().commandId(commandId).targetId(targetId).build();
    }
}
