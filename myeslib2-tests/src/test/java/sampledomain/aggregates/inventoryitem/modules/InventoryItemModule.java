package sampledomain.aggregates.inventoryitem.modules;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import org.myeslib.data.Event;
import org.myeslib.data.SnapshotData;
import org.myeslib.infra.Consumers;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.WriteModelJournal;
import org.myeslib.infra.commandbus.CommandBus;
import org.myeslib.infra.commandbus.CommandSubscriber;
import org.myeslib.stack1.infra.Stack1ApplyEventsFunction;
import org.myeslib.stack1.infra.Stack1InteractionContext;
import org.myeslib.stack1.infra.Stack1Journal;
import org.myeslib.stack1.infra.Stack1SnapshotReader;
import org.myeslib.stack1.infra.commandbus.Stack1CommandBus;
import sampledomain.aggregates.inventoryitem.InventoryItem;
import sampledomain.aggregates.inventoryitem.InventoryItemCmdSubscriber;
import sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import sampledomain.aggregates.inventoryitem.handlers.DecreaseHandler;
import sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;
import sampledomain.services.SampleDomainService;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class InventoryItemModule extends AbstractModule {

    @Provides
    public Supplier<InventoryItem> supplier(SampleDomainService sampleDomainService) {
        return () -> InventoryItem.builder().service(sampleDomainService).build();
    }

    @Provides
    public Function<InventoryItem, InventoryItem> injector() {
        return item -> {
            item.setInteractionContext(new Stack1InteractionContext(item));
            return item;
        };
    }

    @Provides
    @Singleton
    public BiFunction<InventoryItem, List<Event>, InventoryItem> applyEventsFunction() {
        return new Stack1ApplyEventsFunction<>();
    }

    @Override
    protected void configure() {

        // domain service
        bind(SampleDomainService.class).toInstance((id) -> id.toString());

        // command bus
        bind(new TypeLiteral<CommandBus<InventoryItem>>() {
        }).to(new TypeLiteral<Stack1CommandBus<InventoryItem>>() {
                }).asEagerSingleton();
        bind(new TypeLiteral<CommandSubscriber<InventoryItem>>() {})
                .to(new TypeLiteral<InventoryItemCmdSubscriber<InventoryItem>>() {
                }).asEagerSingleton();

        // consumers
        bind(new TypeLiteral<Consumers<InventoryItem>>() {})
                .toInstance(new Consumers<>());

        // command handlers
        bind(CreateInventoryItemHandler.class).asEagerSingleton();
        bind(CreateThenIncreaseThenDecreaseHandler.class).asEagerSingleton();
        bind(IncreaseHandler.class).asEagerSingleton();
        bind(DecreaseHandler.class); // DecreaseHandler is stateful, so it's not thread safe
        bind(InventoryItemCmdSubscriber.class).asEagerSingleton();

        // reader and writer
        bind(new TypeLiteral<SnapshotReader<UUID, InventoryItem>>() {
        }).to(new TypeLiteral<Stack1SnapshotReader<UUID, InventoryItem>>() {
        }).asEagerSingleton();

        bind(new TypeLiteral<Cache<UUID, SnapshotData<InventoryItem>>>() {
        }).toInstance(CacheBuilder.newBuilder().maximumSize(1000).build());

        bind(new TypeLiteral<WriteModelJournal<UUID, InventoryItem>>() {
        }).to(new TypeLiteral<Stack1Journal<UUID, InventoryItem>>() {
        }).asEagerSingleton();

    }

}
