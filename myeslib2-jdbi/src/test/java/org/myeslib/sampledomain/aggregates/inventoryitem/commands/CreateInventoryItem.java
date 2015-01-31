package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Command;

import java.util.UUID;

@AutoValue
public abstract class CreateInventoryItem implements Command {
    public abstract UUID commandId();
    public abstract UUID targetId();
    public static CreateInventoryItem create(UUID commandId, UUID targetId) {
        return new AutoValue_CreateInventoryItem(commandId, targetId);
    }
}
