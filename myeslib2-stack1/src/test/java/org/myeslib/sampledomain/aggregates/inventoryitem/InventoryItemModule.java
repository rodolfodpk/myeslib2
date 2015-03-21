package org.myeslib.sampledomain.aggregates.inventoryitem;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import org.h2.jdbcx.JdbcConnectionPool;
import org.myeslib.data.Command;
import org.myeslib.data.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.Snapshot;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.WriteModelJournal;
import org.myeslib.infra.commandbus.CommandBus;
import org.myeslib.infra.commandbus.CommandSubscriber;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CommandsGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.EventsGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.DecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.myeslib.stack1.infra.*;
import org.myeslib.stack1.infra.commandbus.Stack1CommandBus;
import org.myeslib.stack1.infra.dao.Stack1MemDao;
import org.myeslib.stack1.infra.dao.config.CmdSerialization;
import org.myeslib.stack1.infra.dao.config.DbMetadata;
import org.myeslib.stack1.infra.dao.config.UowSerialization;
import org.myeslib.stack1.infra.helpers.DatabaseHelper;
import org.skife.jdbi.v2.DBI;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class InventoryItemModule extends AbstractModule {

    @Provides
    public Supplier<InventoryItem> supplier() {
        return InventoryItem::new;
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

    @Provides
    @Singleton
    public Consumer<CommandErrorMessage> errorMessageConsumer() {
        return commandErrorMessage -> System.err.println(" ** " + commandErrorMessage);
    }

    @Provides
    @Singleton
    public DatabaseHelper databaseHelper(DBI dbi){
        return new DatabaseHelper(dbi, "database/V1__Create_inventory_item_tables.sql");
    }

    @Provides
    @Named("events-json")
    @Singleton
    public Gson gsonEvents() {
        return new EventsGsonFactory().create();
    }

    @Provides
    @Named("commands-json")
    @Singleton
    public Gson gsonCommands() {
        return new CommandsGsonFactory().create();
    }

    @Provides
    @Singleton
    Cache<UUID, Snapshot<InventoryItem>> cache(){
        return CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    @Provides
    @Singleton
    public DBI dbi() {
        return new DBI(JdbcConnectionPool.create("jdbc:h2:mem:test;MODE=Oracle", "scott", "tiger"));
    }

    @Provides
    @Singleton
    public UowSerialization uowSerialization(@Named("events-json") Gson gson) {
        return new UowSerialization(
                (uow) -> gson.toJson(uow),
                (json) -> gson.fromJson(json, UnitOfWork.class));
    }

    @Provides
    @Singleton
    public CmdSerialization cmdSerialization(@Named("commands-json") Gson gson) {
        return new CmdSerialization(
                (cmd) -> gson.toJson(cmd, Command.class),
                (json) -> gson.fromJson(json, Command.class));
    }

    @Override
    protected void configure() {

        bind(new TypeLiteral<WriteModelDao<UUID>>() {})
                .to(new TypeLiteral<Stack1MemDao<UUID>>() {}).asEagerSingleton();
        bind(new TypeLiteral<WriteModelJournal<UUID>>() {})
                .to(new TypeLiteral<Stack1Journal<UUID>>() {}).asEagerSingleton();
        bind(new TypeLiteral<SnapshotReader<UUID, InventoryItem>>() {})
                .to(new TypeLiteral<Stack1Reader<UUID, InventoryItem>>() {}).asEagerSingleton();

        bind(SampleDomainService.class).toInstance((id) -> id.toString());
        bind(DbMetadata.class).toInstance(new DbMetadata("inventory_item"));

        // command bus
        bind(CommandBus.class).to(Stack1CommandBus.class).asEagerSingleton();
        bind(CommandSubscriber.class).to(InventoryItemCmdSubscriber.class);

        // command handlers
        bind(CreateInventoryItemHandler.class).asEagerSingleton();
        bind(CreateThenIncreaseThenDecreaseHandler.class).asEagerSingleton();
        bind(IncreaseHandler.class).asEagerSingleton();
        bind(DecreaseHandler.class); // DecreaseHandler is stateful, so it's not thread safe
        bind(InventoryItemCmdSubscriber.class).asEagerSingleton();
    }
}
