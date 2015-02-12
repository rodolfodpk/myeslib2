package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;

import java.util.UUID;

@AutoValue
public abstract class CreateInventoryItemThenIncreaseThenDecrease implements Command {

    public abstract CommandId getCommandId();
    public abstract UUID targetId();
    public abstract Integer howManyToIncrease();
    public abstract Integer howManyToDecrease();

    public static CreateInventoryItemThenIncreaseThenDecrease create(CommandId commandId, UUID targetId, Integer howManyIncr, Integer howManyDecr) {
       return new AutoValue_CreateInventoryItemThenIncreaseThenDecrease(commandId, targetId, howManyIncr, howManyDecr) ;
    }

    // @AutoValue.Builder
    static interface Builder {
        Builder commandId(CommandId commandId);
        Builder targetId(UUID targetId);
        Builder increase(Integer increase);
        Builder decrease(Integer decrease);
        CreateInventoryItemThenIncreaseThenDecrease build();
    }

}
