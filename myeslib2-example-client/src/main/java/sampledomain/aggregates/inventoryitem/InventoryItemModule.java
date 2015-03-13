package sampledomain.aggregates.inventoryitem;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.h2.jdbcx.JdbcConnectionPool;
import org.myeslib.data.Command;
import org.myeslib.data.EventMessage;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.ApplyEventsFunction;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.stack1.infra.MultiMethodApplyEventsFunction;
import org.myeslib.stack1.infra.Stack1Journal;
import org.myeslib.stack1.infra.Stack1Reader;
import org.myeslib.stack1.infra.dao.Stack1Dao;
import org.myeslib.stack1.infra.dao.UnitOfWorkDao;
import org.myeslib.stack1.infra.dao.config.CmdSerialization;
import org.myeslib.stack1.infra.dao.config.DbMetadata;
import org.myeslib.stack1.infra.dao.config.UowSerialization;
import org.myeslib.stack1.infra.helpers.DatabaseHelper;
import org.skife.jdbi.v2.DBI;
import sampledomain.aggregates.inventoryitem.commands.CommandsGsonFactory;
import sampledomain.aggregates.inventoryitem.events.EventsGsonFactory;
import sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import sampledomain.aggregates.inventoryitem.handlers.DecreaseHandler;
import sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;
import sampledomain.services.SampleDomainService;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InventoryItemModule extends AbstractModule {

    @Provides
    @Singleton
    public EventBus commandBus(InventoryItemCmdSubscriber subscriber) {
        EventBus eventBus = new EventBus("inventoryItemCommandBus");
        eventBus.register(subscriber);
        return eventBus;
    }

    @Provides
    @Singleton
    public UnitOfWorkJournal<UUID> journal(UnitOfWorkDao<UUID> dao, List<Consumer<EventMessage>> sagaConsumer) {
        return new Stack1Journal<>(dao, sagaConsumer);
    }

    @Provides
    @Singleton
    public SnapshotReader<UUID, InventoryItem> snapshotReader(Supplier<InventoryItem> supplier,
                                                              UnitOfWorkDao<UUID> dao,
                                                          Cache<UUID, Snapshot<InventoryItem>> cache,
                                                          ApplyEventsFunction<InventoryItem> applyEventsFunction,
                                                          Kryo kryo ) {
        return new Stack1Reader<>(supplier, dao, cache, applyEventsFunction, kryo);
    }

    @Provides
    @Singleton
    public UnitOfWorkDao<UUID> dao(UowSerialization uowSer, CmdSerialization cmdSer,
                             DbMetadata dbMetadata, DBI dbi) {
        return new Stack1Dao<>(uowSer, cmdSer, dbMetadata, dbi);
    }

    @Provides
    @Singleton
    public DatabaseHelper databaseHelper(DBI dbi){
        return new DatabaseHelper(dbi, "database/V1__Create_inventory_item_tables.sql");
    }

    @Provides
    @Singleton
    public Kryo kryo() {
        return new Kryo();
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
        bind(SampleDomainService.class).toInstance((id) -> id.toString());
        bind(DbMetadata.class).toInstance(new DbMetadata("inventory_item"));

        // command handlers
        bind(CreateInventoryItemHandler.class).asEagerSingleton();
        bind(CreateThenIncreaseThenDecreaseHandler.class).asEagerSingleton();
        bind(IncreaseHandler.class).asEagerSingleton();
        bind(DecreaseHandler.class); // DecreaseHandler is stateful, so it's not thread safe
        bind(InventoryItemCmdSubscriber.class).asEagerSingleton();
    }
}
