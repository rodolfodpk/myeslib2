package org.myeslib.storage.helpers;

import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Builder;
import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Command;
import org.myeslib.core.CommandHandler;
import org.myeslib.core.Event;
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWork;

import java.util.Arrays;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("serial")
public class SampleDomain {

    public static interface SampleDomainService {
        String generate(UUID id);
    }

    // command handlers

    @Data
    public static class InventoryItemAggregateRoot implements AggregateRoot {

        UUID id;
        String description;
        Integer available = 0;

        public void on(InventoryItemCreated event) {
            this.id = event.id;
            this.description = event.description;
            this.available = 0;
        }

        public void on(InventoryIncreased event) {
            this.available = this.available + event.howMany;
        }

        public void on(InventoryDecreased event) {
            this.available = this.available - event.howMany;
        }

        public boolean isAvailable(int howMany) {
            return getAvailable() - howMany >= 0;
        }

    }

    // commands handlers

    @AllArgsConstructor
    public static class CreateCommandHandler implements CommandHandler<CreateInventoryItem, InventoryItemAggregateRoot> {

        @NonNull
        final SampleDomainService service;

        @Override
        public UnitOfWork handle(CreateInventoryItem command, Snapshot<InventoryItemAggregateRoot> snapshot) {
            checkNotNull(service);
            checkArgument(snapshot.getAggregateInstance().getId() == null, "item already exists");
            String description = service.generate(command.getId());
            InventoryItemCreated event = new InventoryItemCreated(command.getId(), description);
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }
    }

    @AllArgsConstructor
    public static class IncreaseCommandHandler implements CommandHandler<IncreaseInventory, InventoryItemAggregateRoot> {

        public UnitOfWork handle(IncreaseInventory command, Snapshot<InventoryItemAggregateRoot> snapshot) {
            InventoryItemAggregateRoot aggregateRoot = snapshot.getAggregateInstance();
            checkArgument(aggregateRoot.getId() != null, "before increasing you must create an item");
            checkArgument(aggregateRoot.getId().equals(command.getId()), "item id does not match");
            InventoryIncreased event = new InventoryIncreased(command.getId(), command.getHowMany());
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }
    }

    @AllArgsConstructor
    public static class DecreaseCommandHandler implements CommandHandler<DecreaseInventory, InventoryItemAggregateRoot> {

        public UnitOfWork handle(DecreaseInventory command, Snapshot<InventoryItemAggregateRoot> snapshot) {
            InventoryItemAggregateRoot aggregateRoot = snapshot.getAggregateInstance();
            checkArgument(aggregateRoot.getId() != null, "before decreasing you must create an item");
            checkArgument(aggregateRoot.getId().equals(command.getId()), "item id does not match");
            checkArgument(aggregateRoot.isAvailable(command.howMany), "there are not enough items available");
            InventoryDecreased event = new InventoryDecreased(command.getId(), command.getHowMany());
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }
    }

    @AllArgsConstructor
    public static class CreateThenIncreaseAndDecreaseCommandHandler implements CommandHandler<CreateInventoryItemThenIncreaseAndDecrease, InventoryItemAggregateRoot> {

        @NonNull
        final SampleDomainService service;

        @Override
        public UnitOfWork handle(CreateInventoryItemThenIncreaseAndDecrease command, Snapshot<InventoryItemAggregateRoot> snapshot) {
            checkNotNull(service);
            checkArgument(snapshot.getAggregateInstance().getId() == null, "item already exists");
            String description = service.generate(command.getId());
            InventoryItemCreated event1 = new InventoryItemCreated(command.getId(), description);
            InventoryIncreased event2 = new InventoryIncreased(command.getId(), command.getHowManyToIncrease());
            InventoryDecreased event3 = new InventoryDecreased(command.getId(), command.getHowManyToDecrease());
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event1, event2, event3));
        }
    }

    // commands

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


    @Value
    public static class InventoryDecreased implements Event {
        @NonNull
        UUID id;
        @NonNull
        Integer howMany;
    }

    @Value
    public static class CreateInventoryItemThenIncreaseAndDecrease implements Command {
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

}
