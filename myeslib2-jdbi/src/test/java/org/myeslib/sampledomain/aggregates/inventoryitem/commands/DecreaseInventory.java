package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Command;
import org.myeslib.jdbi.core.JdbiCommandId;

import java.util.UUID;

@AutoValue
public abstract class DecreaseInventory implements Command {
    public abstract JdbiCommandId commandId();
    public abstract UUID targetId();
    public abstract Integer howMany();
    public static DecreaseInventory create(JdbiCommandId commandId, UUID targetId, Integer howMany) {
        return new AutoValue_DecreaseInventory(commandId, targetId, howMany) ;
    }
}
