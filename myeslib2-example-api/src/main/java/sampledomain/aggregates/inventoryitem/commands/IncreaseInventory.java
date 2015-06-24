package sampledomain.aggregates.inventoryitem.commands;

import org.immutables.value.Value;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;

import java.util.UUID;

@Value.Immutable
@Value.Style(strictBuilder = true)
public abstract class IncreaseInventory implements Command {
    public abstract CommandId getCommandId();
    public abstract UUID targetId();
    public abstract Integer howMany();
    public static IncreaseInventory create(CommandId commandId, UUID targetId, Integer howMany) {
        return ImmutableIncreaseInventory.builder().commandId(commandId).targetId(targetId).howMany(howMany).build();
    }
}
