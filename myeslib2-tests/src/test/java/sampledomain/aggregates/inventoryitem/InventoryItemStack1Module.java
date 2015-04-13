package sampledomain.aggregates.inventoryitem;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import org.myeslib.data.Command;
import org.myeslib.data.Event;
import org.myeslib.data.EventMessage;
import org.myeslib.infra.Consumers;
import org.myeslib.infra.WriteModelJournal;
import org.myeslib.infra.commandbus.CommandBus;
import org.myeslib.infra.commandbus.CommandSubscriber;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;
import org.myeslib.stack1.infra.*;
import org.myeslib.stack1.infra.commandbus.Stack1CommandBus;
import sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import sampledomain.aggregates.inventoryitem.handlers.DecreaseHandler;
import sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;
import sampledomain.services.SampleDomainService;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class InventoryItemStack1Module extends AbstractModule {

    @Provides
    public Supplier<InventoryItem> supplier() {
        return () -> InventoryItem.builder().build();
    }

    @Provides
    public Function<InventoryItem, InventoryItem> injectorFunction(SampleDomainService sampleDomainService) {
        return item -> {
            item.setService(sampleDomainService);
            item.setInteractionContext(new Stack1InteractionContext(item));
            return item;
        };
    }

    @Provides
    @Singleton
    public SnapshotFactory<InventoryItem> snapshotFactory(final Supplier<InventoryItem> supplier,
                                                          final Function<InventoryItem, InventoryItem> injectorFunction) {
        return (eventSourced, version) -> new Stack1Snapshot<>(eventSourced, version, supplier, injectorFunction);
    }

    @Provides
    @Singleton
    public BiFunction<InventoryItem, List<Event>, InventoryItem> applyEventsFunction() {
        return new Stack1ApplyEventsFunction<>();
    }

    @Override
    protected void configure() {

        bind(SampleDomainService.class).toInstance((id) -> id.toString());

        bind(new TypeLiteral<Consumers<InventoryItem>>() {})
                .toInstance(new InventoryItemConsumers());

        // command bus
        bind(new TypeLiteral<CommandBus<InventoryItem>>() {
        })
                .to(new TypeLiteral<Stack1CommandBus<InventoryItem>>() {
                }).asEagerSingleton();
        bind(new TypeLiteral<CommandSubscriber<InventoryItem>>() {})
                .to(new TypeLiteral<InventoryItemCmdSubscriber<InventoryItem>>() {
                }).asEagerSingleton();

        // command handlers
        bind(CreateInventoryItemHandler.class).asEagerSingleton();
        bind(CreateThenIncreaseThenDecreaseHandler.class).asEagerSingleton();
        bind(IncreaseHandler.class).asEagerSingleton();
        bind(DecreaseHandler.class); // DecreaseHandler is stateful, so it's not thread safe
        bind(InventoryItemCmdSubscriber.class).asEagerSingleton();
    }

    public static class InventoryItemConsumers implements Consumers<InventoryItem> {

        @Override
        public List<Consumer<List<EventMessage>>> eventMessageConsumers() {
            return Arrays.asList((eventList) -> System.out.println("received " + eventList));
        }

        @Override
        public List<Consumer<List<Command>>> commandsConsumers() {
            return Arrays.asList((commandList) -> System.out.println("received " + commandList));
        }

        @Override
        public List<Consumer<CommandErrorMessage>> errorMessageConsumers() {
            return Arrays.asList((error) -> System.out.println("received " + error));
        }
    }
}
