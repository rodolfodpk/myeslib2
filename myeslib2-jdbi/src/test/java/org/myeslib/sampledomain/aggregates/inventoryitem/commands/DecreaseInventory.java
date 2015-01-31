package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Command;

import java.util.UUID;

@AutoValue
public abstract class DecreaseInventory implements Command {
    public abstract UUID commandId();
    public abstract UUID targetId();
    public abstract Integer howMany();
    public static DecreaseInventory create(UUID commandId, UUID targetId, Integer howMany) {
        return new AutoValue_DecreaseInventory(commandId, targetId, howMany) ;
    }
}
