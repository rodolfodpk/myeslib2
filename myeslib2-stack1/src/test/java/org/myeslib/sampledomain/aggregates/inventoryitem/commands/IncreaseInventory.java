package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Command;
import org.myeslib.core.CommandId;

import java.util.UUID;

@AutoValue
public abstract class IncreaseInventory implements Command {
    IncreaseInventory() {}
    public abstract CommandId getCommandId();
    public abstract UUID targetId();
    public abstract Integer howMany();
    public static IncreaseInventory create(CommandId commandId, UUID targetId, Integer howMany) {
        return new AutoValue_IncreaseInventory(commandId, targetId, howMany) ;
    }
}
