package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import lombok.NonNull;
import lombok.Value;
import org.myeslib.core.Command;

import java.util.UUID;

@Value
public class DecreaseInventory implements Command<UUID> {
    @NonNull
    UUID commandId;
    @NonNull
    UUID targetId;
    @NonNull
    Integer howMany;
}
