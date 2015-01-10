package org.myeslib.sampledomain.aggregates.inventoryitem;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Builder;
import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.domain.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.domain.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.domain.InventoryItemCreated;
import org.myeslib.sampledomain.services.SampleDomainService;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Builder @EqualsAndHashCode @ToString(exclude = "service")
public class InventoryItem implements AggregateRoot {

    private transient SampleDomainService service;

    private UUID id;
    private String description;
    private Integer available = 0;

    // domain behaviour (ok, it is coupled with Event interface but...)

    public Event create(UUID id) {
        isNew();
        hasAllRequiredServices();
        return InventoryItemCreated.create(id, service.generateItemDescription(id));
    }

    public Event increase(int howMany) {
        isCreated();
        return InventoryIncreased.create(howMany);
    }

    public Event decrease(int howMany) {
        isCreated();
        checkArgument(howMany <= available, "there aren't enough items available");
        return InventoryDecreased.create(howMany);
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
        this.id = event.id();
        this.description = event.description();
        this.available = 0;
    }

    public void on(InventoryIncreased event) {
        this.available = this.available + event.howMany();
    }

    public void on(InventoryDecreased event) {
        this.available = this.available - event.howMany();
    }

    // setters

    public void setService(SampleDomainService service) {
        this.service = service;
    }

    // static factories

//        public static InventoryItem create(UUID id, String description, Integer available) {
//            return
//        }

}
