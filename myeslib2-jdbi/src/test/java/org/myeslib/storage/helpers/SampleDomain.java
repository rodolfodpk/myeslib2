package org.myeslib.storage.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.Value;
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

        transient SampleDomainService service;

        UUID id;
        String description;
        Integer available = 0;

        // domain behaviour (ok, it is coupled with Event interface but...)

        public Event create(UUID id) {
            isNew();
            hasAllRequiredServices();
            return new InventoryItemCreated(id, service.generate(id));
        }

        public Event increase(int howMany) {
            isCreated();
            return new InventoryIncreased(howMany);
        }

        public Event decrease(int howMany) {
            isCreated();
            checkArgument(howMany <= available, "there aren't enough items available");
            return new InventoryDecreased(howMany);
        }

        // guards

        private void isNew() {
            checkArgument(this.id == null, "item already exists");
        }

        private void hasAllRequiredServices() {
            checkNotNull(service, "SampleDomainService cannot be null");
        }

        private void isCreated() {
            checkArgument(id != null, "This item is not created and no operations can be executed on it");
        }

        // events handlers (reflect the state)

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

    }

    // commands handlers

    @AllArgsConstructor
    public static class CreateCommandHandler implements CommandHandler<CreateInventoryItem, InventoryItemAggregateRoot> {

        @NonNull
        final SampleDomainService service;

        @Override
        public UnitOfWork handle(CreateInventoryItem command, Snapshot<InventoryItemAggregateRoot> snapshot) {
            final InventoryItemAggregateRoot aggregateRoot = snapshot.getAggregateInstance();
            aggregateRoot.setService(service); // instead, it could be using Guice to inject necessary services
            final Event event = aggregateRoot.create(command.getId());
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }
    }

    @AllArgsConstructor
    public static class IncreaseCommandHandler implements CommandHandler<IncreaseInventory, InventoryItemAggregateRoot> {

        public UnitOfWork handle(IncreaseInventory command, Snapshot<InventoryItemAggregateRoot> snapshot) {
            final InventoryItemAggregateRoot aggregateRoot = snapshot.getAggregateInstance();
            final Event event = aggregateRoot.increase(command.getHowMany());
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }
    }

    @AllArgsConstructor
    public static class DecreaseCommandHandler implements CommandHandler<DecreaseInventory, InventoryItemAggregateRoot> {

        public UnitOfWork handle(DecreaseInventory command, Snapshot<InventoryItemAggregateRoot> snapshot) {
            final InventoryItemAggregateRoot aggregateRoot = snapshot.getAggregateInstance();
            final Event event = aggregateRoot.increase(command.getHowMany());
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
            InventoryIncreased event2 = new InventoryIncreased(command.getHowManyToIncrease());
            InventoryDecreased event3 = new InventoryDecreased(command.getHowManyToDecrease());
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
    public static class InventoryItemCreated implements Event {
        @NonNull
        UUID id;
        @NonNull
        String description;
    }

    @Value
    public static class InventoryIncreased implements Event {
        @NonNull
        Integer howMany;
    }

    @Value
    public static class InventoryDecreased implements Event {
        @NonNull
        Integer howMany;
    }

}
