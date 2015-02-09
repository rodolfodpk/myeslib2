package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import com.google.auto.value.AutoValue;
import org.myeslib.core.Command;
import org.myeslib.core.CommandId;
import org.myeslib.stack1.core.Stack1CommandId;

import java.util.UUID;

@AutoValue
public abstract class CreateInventoryItemThenIncreaseThenDecrease implements Command {

    public abstract Stack1CommandId commandId();
    public abstract UUID targetId();
    public abstract Integer howManyToIncrease();
    public abstract Integer howManyToDecrease();

    public static CreateInventoryItemThenIncreaseThenDecrease create(Stack1CommandId commandId, UUID targetId, Integer howManyIncr, Integer howManyDecr) {
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
