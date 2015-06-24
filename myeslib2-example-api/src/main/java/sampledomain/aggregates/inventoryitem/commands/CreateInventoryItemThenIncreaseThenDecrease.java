package sampledomain.aggregates.inventoryitem.commands;

import org.immutables.value.Value;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;

import java.util.UUID;

@Value.Immutable
@Value.Style(strictBuilder = true)
public abstract class CreateInventoryItemThenIncreaseThenDecrease implements Command {

    public abstract CommandId getCommandId();
    public abstract UUID targetId();
    public abstract Integer howManyToIncrease();
    public abstract Integer howManyToDecrease();

    public static CreateInventoryItemThenIncreaseThenDecrease create(CommandId commandId, UUID targetId, Integer howManyIncr, Integer howManyDecr) {
       return ImmutableCreateInventoryItemThenIncreaseThenDecrease.builder()
               .commandId(commandId).targetId(targetId).howManyToIncrease(howManyIncr).howManyToDecrease(howManyDecr).build();
    }

}
