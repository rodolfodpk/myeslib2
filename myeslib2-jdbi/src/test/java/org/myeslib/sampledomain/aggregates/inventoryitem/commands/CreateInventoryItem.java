package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import lombok.NonNull;
import lombok.Value;
import org.myeslib.core.Command;

import java.util.UUID;

@Value
public class CreateInventoryItem implements Command<UUID> {
    @NonNull
    UUID commandId;
    @NonNull
    UUID targetId;
}
