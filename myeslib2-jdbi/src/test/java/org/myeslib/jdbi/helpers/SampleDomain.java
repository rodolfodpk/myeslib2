package org.myeslib.jdbi.helpers;

import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.Value;
import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Command;
import org.myeslib.core.CommandHandler;
import org.myeslib.core.Event;
import org.myeslib.core.data.UnitOfWork;

import java.util.Arrays;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("serial")
public class SampleDomain {

    public static interface ItemDescriptionGeneratorService {
        String generate(UUID id);
    }

    // command handlers

    @Data
    public static class InventoryItemAggregateRoot implements AggregateRoot {

        UUID id;
        String description;
        Integer available = 0;

        @Subscribe
        public void on(InventoryItemCreated event) {
            this.id = event.id;
            this.description = event.description;
            this.available = 0;
        }

        @Subscribe
        public void on(InventoryIncreased event) {
            this.available = this.available + event.howMany;
        }

        @Subscribe
        public void on(InventoryDecreased event) {
            this.available = this.available - event.howMany;
        }

        public boolean isAvailable(int howMany) {
            return getAvailable() - howMany >= 0;
        }

    }

    @AllArgsConstructor
    public static class CreateCommandHandler implements CommandHandler<CreateInventoryItem> {

        @NonNull
        final InventoryItemAggregateRoot aggregateRoot;
        @NonNull
        final ItemDescriptionGeneratorService service;

        public org.myeslib.core.data.UnitOfWork handle(CreateInventoryItem command) {
            checkArgument(aggregateRoot.getId() == null, "item already exists");
            checkNotNull(service);
            String description = service.generate(command.getId());
            InventoryItemCreated event = new InventoryItemCreated(command.getId(), description);
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }
    }

    @AllArgsConstructor
    public static class IncreaseCommandHandler implements CommandHandler<IncreaseInventory> {

        @NonNull
        final InventoryItemAggregateRoot aggregateRoot;

        public org.myeslib.core.data.UnitOfWork handle(IncreaseInventory command) {
            checkArgument(aggregateRoot.getId() != null, "before increasing you must create an item");
            checkArgument(aggregateRoot.getId().equals(command.getId()), "item id does not match");
            InventoryIncreased event = new InventoryIncreased(command.getId(), command.getHowMany());
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }
    }

    // commands

    @AllArgsConstructor
    public static class DecreaseCommandHandler implements CommandHandler<DecreaseInventory> {

        @NonNull
        final InventoryItemAggregateRoot aggregateRoot;

        public org.myeslib.core.data.UnitOfWork handle(DecreaseInventory command) {
            checkArgument(aggregateRoot.getId() != null, "before decreasing you must create an item");
            checkArgument(aggregateRoot.getId().equals(command.getId()), "item id does not match");
            checkArgument(aggregateRoot.isAvailable(command.howMany), "there are not enough items available");
            InventoryDecreased event = new InventoryDecreased(command.getId(), command.getHowMany());
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }
    }

    @Value
    public static class CreateInventoryItem implements Command {
        final Long targetVersion = 0L;
        @NonNull
        UUID commandId;
        @NonNull
        UUID id;
    }

    @Value
    public static class IncreaseInventory implements Command {
        @NonNull
        UUID commandId;
        @NonNull
        UUID id;
        @NonNull
        Integer howMany;
        Long targetVersion;
    }

    // events

    @Value
    public static class DecreaseInventory implements Command {
        @NonNull
        UUID commandId;
        @NonNull
        UUID id;
        @NonNull
        Integer howMany;
        Long targetVersion;
    }

    @Value
    public static class InventoryItemCreated implements Event {
        @NonNull
        UUID id;
        @NonNull
        String description;
    }

    @Value
    public static class InventoryIncreased implements Event {
        @NonNull
        UUID id;
        @NonNull
        Integer howMany;
    }

    // a service just for the sake of the example

    @Value
    public static class InventoryDecreased implements Event {
        @NonNull
        UUID id;
        @NonNull
        Integer howMany;
    }


}
