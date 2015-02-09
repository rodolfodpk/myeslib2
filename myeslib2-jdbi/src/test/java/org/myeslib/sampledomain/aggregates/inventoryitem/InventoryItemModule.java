package org.myeslib.sampledomain.aggregates.inventoryitem;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.h2.jdbcx.JdbcConnectionPool;
import org.myeslib.core.Command;
import org.myeslib.core.CommandId;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.jdbi.core.JdbiCommandId;
import org.myeslib.jdbi.data.JdbiUnitOfWork;
import org.myeslib.infra.ApplyEventsFunction;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.jdbi.infra.JdbiJournal;
import org.myeslib.jdbi.infra.JdbiReader;
import org.myeslib.jdbi.infra.MultiMethodApplyEventsFunction;
import org.myeslib.jdbi.infra.dao.JdbiDao;
import org.myeslib.jdbi.infra.dao.UnitOfWorkDao;
import org.myeslib.jdbi.infra.dao.config.CmdSerialization;
import org.myeslib.jdbi.infra.dao.config.DbMetadata;
import org.myeslib.jdbi.infra.dao.config.UowSerialization;
import org.myeslib.jdbi.infra.helpers.DatabaseHelper;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CommandsGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.EventsGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.DecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.skife.jdbi.v2.DBI;

import java.util.UUID;
import java.util.function.Supplier;

public class InventoryItemModule extends PrivateModule {

    @Provides
    @Exposed
    @Singleton
    @Named("inventory-item-cmd-bus")
    public EventBus commandBus(InventoryItemCmdSubscriber subscriber) {
        EventBus eventBus = new EventBus("inventoryItemCommandBus");
        eventBus.register(subscriber);
        return eventBus;
    }

    @Provides
    @Exposed
    @Singleton
    public UnitOfWorkJournal<UUID> journal(UnitOfWorkDao<UUID> dao, EventBus[] eventSubscribers) {
        return new JdbiJournal<UUID>(dao, eventSubscribers);
    }

    @Provides
    @Exposed
    @Singleton
    public EventBus[] eventSubscribers() {
        return new EventBus[] { new EventBus("query-model-bus"), new EventBus("saga1-process-bus")};
    }

    @Provides
    @Exposed
    @Singleton
    public SnapshotReader<UUID, InventoryItem> snapshotReader(Supplier<InventoryItem> supplier,
                                                              UnitOfWorkDao<UUID> dao,
                                                          Cache<UUID, Snapshot<InventoryItem>> cache,
                                                          ApplyEventsFunction<InventoryItem> applyEventsFunction){
        return new JdbiReader<>(supplier, dao, cache, applyEventsFunction);
    }

    @Provides
    @Exposed
    @Singleton
    public UnitOfWorkDao<UUID> dao(UowSerialization uowSer, CmdSerialization cmdSer,
                             DbMetadata dbMetadata, DBI dbi) {
        return new JdbiDao<>(uowSer, cmdSer, dbMetadata, dbi);
    }

    @Provides
    @Exposed
    @Singleton
    public DatabaseHelper databaseHelper(DBI dbi){
        return new DatabaseHelper(dbi, "database/V1__Create_inventory_item_tables.sql");
    }

    @Provides
    @Exposed
    @Singleton
    public Supplier<UnitOfWorkId> supplierUowId() {
        return () -> null ; //JdbiUnitOfWork.create(UUID.randomUUID());
    }

    @Provides
    @Exposed
    @Singleton
    public Supplier<CommandId> supplierCmdId() {
        return () -> JdbiCommandId.create(UUID.randomUUID());
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
    public Supplier<InventoryItem> supplier() { return () -> InventoryItem.builder().build(); }

    @Provides
    @Singleton
    public ApplyEventsFunction<InventoryItem> applyEventsFunction() {
        return new MultiMethodApplyEventsFunction<>();
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
                (json) -> gson.fromJson(json, JdbiUnitOfWork.class));
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
        bind(SampleDomainService.class).toInstance((id) -> id.toString());
        expose(SampleDomainService.class);
        bind(DbMetadata.class).toInstance(new DbMetadata("inventory_item"));

        // command handlers
        bind(CreateInventoryItemHandler.class).asEagerSingleton();
        expose(CreateInventoryItemHandler.class);
        bind(CreateThenIncreaseThenDecreaseHandler.class).asEagerSingleton();
        expose(CreateThenIncreaseThenDecreaseHandler.class);
        bind(IncreaseHandler.class).asEagerSingleton();
        expose(IncreaseHandler.class);
        bind(DecreaseHandler.class); // DecreaseHandler is stateful, so it's not thread safe
        expose(DecreaseHandler.class);
        bind(InventoryItemCmdSubscriber.class).asEagerSingleton();
        expose(InventoryItemCmdSubscriber.class);
    }
}
