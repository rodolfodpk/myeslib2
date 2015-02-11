package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Command;
import org.myeslib.core.CommandId;

import java.util.UUID;

@AutoValue
public abstract class CreateInventoryItem implements Command {
    public abstract CommandId getCommandId();
    public abstract UUID targetId();
    public static CreateInventoryItem create(CommandId commandId, UUID targetId) {
        return new AutoValue_CreateInventoryItem(commandId, targetId);
    }
}
