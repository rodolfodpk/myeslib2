package sampledomain.aggregates.inventoryitem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Builder;
import org.myeslib.core.AggregateRoot;
import org.myeslib.infra.InteractionContext;
import sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import sampledomain.services.SampleDomainService;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


@Getter @Setter @Builder
@EqualsAndHashCode(exclude = {"service", "interactionContext"})  @ToString(exclude = {"service", "interactionContext"})
public class InventoryItem implements AggregateRoot {

    public UUID id;
    public String description;
    public Integer available = 0;

    transient SampleDomainService service;
    transient InteractionContext interactionContext;

    // domain behaviour

    public void create(UUID id) {
        isNew();
        hasAllRequiredServices();
        emit(InventoryItemCreated.create(id, service.generateItemDescription(id)));
    }

    public void increase(int howMany) {
        isCreated();
        emit(InventoryIncreased.create(howMany));
    }

    public void decrease(int howMany) {
        isCreated();
        checkArgument(howMany <= available, "there aren't enough items available");
        emit(InventoryDecreased.create(howMany));
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

}
