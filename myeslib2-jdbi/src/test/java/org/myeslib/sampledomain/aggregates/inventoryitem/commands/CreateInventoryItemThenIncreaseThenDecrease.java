package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import lombok.NonNull;
import lombok.Value;
import org.myeslib.core.Command;

import java.util.UUID;

@Value
public class CreateInventoryItemThenIncreaseThenDecrease implements Command {
    final Long targetVersion = 0L;
    @NonNull
    UUID commandId;
    @NonNull
    UUID id;
    @NonNull
    Integer howManyToIncrease;
    @NonNull
    Integer howManyToDecrease;
}
