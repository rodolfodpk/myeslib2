package org.myeslib.storage.helpers;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
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

    // domain services

    public static interface SampleDomainService {
        String generate(UUID id);
    }

    // aggregate root

    @Builder
    public static class InventoryItem implements AggregateRoot {

        private transient SampleDomainService service;

        private UUID id;
        private String description;
        private Integer available = 0;

        // static factories

//        public static InventoryItem create(UUID id, String description, Integer available) {
//            return
//        }

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

        // setters

        public void setService(SampleDomainService service) {
            this.service = service;
        }

        // Java boilerplate

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InventoryItem that = (InventoryItem) o;

            return Objects.equal(this.service, that.service) &&
                    Objects.equal(this.id, that.id) &&
                    Objects.equal(this.description, that.description) &&
                    Objects.equal(this.available, that.available);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(service, id, description, available);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("service", service)
                    .add("id", id)
                    .add("description", description)
                    .add("available", available)
                    .toString();
        }

    }

    // commands handlers

    @AllArgsConstructor
    public static class HandleCreateInventoryItem implements CommandHandler<CreateInventoryItem, InventoryItem> {

        @NonNull
        final SampleDomainService service;

        @Override
        public UnitOfWork handle(CreateInventoryItem command, Snapshot<InventoryItem> snapshot) {
            final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
            aggregateRoot.setService(service); // instead, it could be using Guice to inject necessary services
            final Event event = aggregateRoot.create(command.getId());
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }
    }

    @AllArgsConstructor
    public static class HandleIncrease implements CommandHandler<IncreaseInventory, InventoryItem> {

        public UnitOfWork handle(IncreaseInventory command, Snapshot<InventoryItem> snapshot) {
            final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
            final Event event = aggregateRoot.increase(command.getHowMany());
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }
    }

    @AllArgsConstructor
    public static class HandleDecrease implements CommandHandler<DecreaseInventory, InventoryItem> {

        public UnitOfWork handle(DecreaseInventory command, Snapshot<InventoryItem> snapshot) {
            final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
            final Event event = aggregateRoot.decrease(command.getHowMany());
            return UnitOfWork.create(UUID.randomUUID(), command, Arrays.asList(event));
        }

    }

    @AllArgsConstructor
    public static class HandleCreateThenIncreaseThenDecrease implements CommandHandler<CreateInventoryItemThenIncreaseThenDecrease, InventoryItem> {

        @NonNull
        final SampleDomainService service;

        @Override
        public UnitOfWork handle(CreateInventoryItemThenIncreaseThenDecrease command, Snapshot<InventoryItem> snapshot) {
            final InventoryItem aggregateRoot = snapshot.getAggregateInstance();
            aggregateRoot.setService(service); // instead, it could be using Guice to inject necessary services
            final Event event1 = aggregateRoot.create(command.getId());
            final Event event2 = aggregateRoot.increase(command.getHowManyToIncrease());
            final Event event3 = aggregateRoot.decrease(command.getHowManyToDecrease());
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
    public static class CreateInventoryItemThenIncreaseThenDecrease implements Command {
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
