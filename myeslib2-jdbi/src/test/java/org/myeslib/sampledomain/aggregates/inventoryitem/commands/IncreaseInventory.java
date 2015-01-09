package org.myeslib.sampledomain.aggregates.inventoryitem.commands;

import lombok.NonNull;
import lombok.Value;
import org.myeslib.core.Command;

import java.util.UUID;

@Value
public class IncreaseInventory implements Command {
    @NonNull
    UUID commandId;
    @NonNull
    UUID id;
    @NonNull
    Integer howMany;
    Long targetVersion;
}
