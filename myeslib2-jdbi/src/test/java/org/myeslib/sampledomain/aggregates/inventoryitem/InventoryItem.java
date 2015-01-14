package org.myeslib.sampledomain.aggregates.inventoryitem;

import com.google.common.eventbus.Subscribe;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Builder;
import org.myeslib.core.AggregateRoot;
import org.myeslib.jdbi.function.StatefulEventBus;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import org.myeslib.sampledomain.services.SampleDomainService;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Builder @EqualsAndHashCode(exclude = {"service", "bus"})  @ToString(exclude = {"service", "bus"})
public class InventoryItem implements AggregateRoot {

    private UUID id;
    private String description;
    private Integer available = 0;

    private transient SampleDomainService service;
    private transient StatefulEventBus bus;

    // domain behaviour

    public void create(UUID id) {
        isNew();
        hasAllRequiredServices();
        bus.post(InventoryItemCreated.create(id, service.generateItemDescription(id)));
    }

    public void increase(int howMany) {
        isCreated();
        bus.post(InventoryIncreased.create(howMany));
    }

    public void decrease(int howMany) {
        isCreated();
        checkArgument(howMany <= available, "there aren't enough items available");
        bus.post(InventoryDecreased.create(howMany));;
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

    // setters

    public void setService(SampleDomainService service) {
        this.service = service;
    }

    public void setBus(StatefulEventBus bus) {
        this.bus = bus;
    }

    // static factories

//        public static InventoryItem create(UUID id, String description, Integer available) {
//            return
//        }

}
