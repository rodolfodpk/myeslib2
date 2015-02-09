package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Command;
import org.myeslib.stack1.core.Stack1CommandId;

import java.util.UUID;

@AutoValue
public abstract class IncreaseInventory implements Command {
    IncreaseInventory() {}
    public abstract Stack1CommandId commandId();
    public abstract UUID targetId();
    public abstract Integer howMany();
    public static IncreaseInventory create(Stack1CommandId commandId, UUID targetId, Integer howMany) {
        return new AutoValue_IncreaseInventory(commandId, targetId, howMany) ;
    }
}
