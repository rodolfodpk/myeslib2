package sampledomain.aggregates.inventoryitem;

import com.google.common.eventbus.Subscribe;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Builder;
import org.myeslib.core.AggregateRoot;
import sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import sampledomain.services.SampleDomainService;
import org.myeslib.infra.InteractionContext;

import javax.inject.Inject;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Builder @Getter
@EqualsAndHashCode(exclude = {"service", "interactionContext"})  @ToString(exclude = {"service", "interactionContext"})
public class InventoryItem implements AggregateRoot {

    private UUID id;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    private String description;
    private Integer available = 0;

    private transient SampleDomainService service;
    private transient InteractionContext interactionContext;

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

    @Inject
    public void setService(SampleDomainService service) {
        this.service = service;
    }

    public void setInteractionContext(InteractionContext interactionContext) {
        this.interactionContext = interactionContext;
    }

    // static factories

//        public static InventoryItem create(UUID id, String description, Integer available) {
//            return
//        }

}
