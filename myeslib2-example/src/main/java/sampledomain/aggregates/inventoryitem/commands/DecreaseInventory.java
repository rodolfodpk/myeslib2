package sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;

import java.util.UUID;

@AutoValue
public abstract class DecreaseInventory implements Command {
    public abstract CommandId getCommandId();
    public abstract UUID targetId();
    public abstract Integer howMany();
    public static DecreaseInventory create(CommandId commandId, UUID targetId, Integer howMany) {
        return new AutoValue_DecreaseInventory(commandId, targetId, howMany) ;
    }
}
