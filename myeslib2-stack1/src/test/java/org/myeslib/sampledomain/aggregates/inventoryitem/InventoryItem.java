package org.myeslib.sampledomain.aggregates.inventoryitem;

import com.google.common.eventbus.Subscribe;
import lombok.*;
import org.myeslib.core.AggregateRoot;
import org.myeslib.infra.InteractionContext;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import org.myeslib.sampledomain.services.SampleDomainService;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter @Setter @AllArgsConstructor
@EqualsAndHashCode(exclude = {"service", "interactionContext"})  @ToString(exclude = {"service", "interactionContext"})
public class InventoryItem implements AggregateRoot {

    public UUID id;
    public String description;
    public Integer available = 0;

    public transient SampleDomainService service;
    public transient InteractionContext interactionContext;

    public InventoryItem() {}

    // domain behaviour

    public void create(UUID id) {
        isNew();
        hasAllRequiredServices();
        interactionContext.apply(InventoryItemCreated.create(id, service.generateItemDescription(id)));
    }

    public void increase(int howMany) {
        isCreated();
        interactionContext.apply(InventoryIncreased.create(howMany));
    }

    public void decrease(int howMany) {
        isCreated();
        checkArgument(howMany <= available, "there aren't enough items available");
        interactionContext.apply(InventoryDecreased.create(howMany));
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

    // events handlers

    @Subscribe
    public void on(InventoryItemCreated event) {
        this.id = event.id();
        this.description = event.description();
        this.available = 0;
    }

    @Subscribe
    public void on(InventoryIncreased event) {
        this.available = this.available + event.howMany();
    }

    @Subscribe
    public void on(InventoryDecreased event) {
        this.available = this.available - event.howMany();
    }

}
