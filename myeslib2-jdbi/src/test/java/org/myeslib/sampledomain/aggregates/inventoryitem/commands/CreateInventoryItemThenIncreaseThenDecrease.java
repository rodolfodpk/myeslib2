package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import lombok.NonNull;
import lombok.Value;
import org.myeslib.core.Command;
import org.myeslib.core.CommandId;

import java.util.UUID;

@AutoValue
public abstract class CreateInventoryItemThenIncreaseThenDecrease implements Command {
    public abstract CommandId commandId();
    public abstract UUID targetId();
    public abstract Integer howManyToIncrease();
    public abstract Integer howManyToDecrease();
    public static CreateInventoryItemThenIncreaseThenDecrease create(CommandId commandId, UUID targetId, Integer howManyIncr, Integer howManyDecr) {
        return new AutoValue_CreateInventoryItemThenIncreaseThenDecrease(commandId, targetId, howManyIncr, howManyDecr) ;
    }
}
