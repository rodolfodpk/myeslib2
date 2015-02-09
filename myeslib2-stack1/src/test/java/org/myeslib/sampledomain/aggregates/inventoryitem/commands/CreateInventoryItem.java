package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Command;
import org.myeslib.stack1.core.Stack1CommandId;

import java.util.UUID;

@AutoValue
public abstract class CreateInventoryItem implements Command {
    public abstract Stack1CommandId commandId();
    public abstract UUID targetId();
    public static CreateInventoryItem create(Stack1CommandId commandId, UUID targetId) {
        return new AutoValue_CreateInventoryItem(commandId, targetId);
    }
}
