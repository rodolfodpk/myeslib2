package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Command;
import org.myeslib.stack1.core.Stack1CommandId;

import java.util.UUID;

@AutoValue
public abstract class DecreaseInventory implements Command {
    public abstract Stack1CommandId commandId();
    public abstract UUID targetId();
    public abstract Integer howMany();
    public static DecreaseInventory create(Stack1CommandId commandId, UUID targetId, Integer howMany) {
        return new AutoValue_DecreaseInventory(commandId, targetId, howMany) ;
    }
}
