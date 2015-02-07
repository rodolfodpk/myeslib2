package org.myeslib.sampledomain.aggregates.inventoryitem;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import org.h2.jdbcx.JdbcConnectionPool;
import org.myeslib.core.Command;
import org.myeslib.core.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
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
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.DecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.skife.jdbi.v2.DBI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.function.Supplier;

public class InventoryItemModuleKryo extends PrivateModule {

    @Provides
    @Exposed
    public EventBus commandBus(InventoryItemCmdSubscriber subscriber) {
        EventBus eventBus = new EventBus("inventoryItemCommandBus");
        eventBus.register(subscriber);
        return eventBus;
    }

    @Provides
    @Exposed
    public UnitOfWorkJournal<UUID> journal(UnitOfWorkDao<UUID> dao) {
        return new JdbiJournal<>(dao);
    }

    @Provides
    @Exposed
    public SnapshotReader<UUID, InventoryItem> snapshotReader(Supplier<InventoryItem> supplier,
                                                              UnitOfWorkDao<UUID> dao,
                                                          Cache<UUID, Snapshot<InventoryItem>> cache,
                                                          ApplyEventsFunction<InventoryItem> applyEventsFunction){
        return new JdbiReader<>(supplier, dao, cache, applyEventsFunction);
    }

    @Provides
    @Exposed
    public UnitOfWorkDao<UUID> dao(UowSerialization uowSer, CmdSerialization cmdSer,
                             DbMetadata dbMetadata, DBI dbi) {
        return new JdbiDao<>(uowSer, cmdSer, dbMetadata, dbi);
    }

    @Provides
    @Exposed
    public DatabaseHelper databaseHelper(DBI dbi){
        return new DatabaseHelper(dbi, "database/V1__Create_inventory_item_tables.sql");
    }

    @Provides
    public Kryo kryo() {
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

        kryo.register(Command.class);
        kryo.register(CreateInventoryItem.class);
        kryo.register(IncreaseInventory.class);
        kryo.register(DecreaseInventory.class);
        kryo.register(CreateThenIncreaseThenDecreaseHandler.class);

        kryo.register(Event.class);
        kryo.register(InventoryItemCreated.class);
        kryo.register(InventoryIncreased.class);
        kryo.register(InventoryDecreased.class);

        kryo.register(UnitOfWork.class);

        return kryo;
    }

    @Provides
    Cache<UUID, Snapshot<InventoryItem>> cache(){
        return CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    @Provides
    public Supplier<InventoryItem> supplier() { return () -> InventoryItem.builder().build(); }

    @Provides
    public ApplyEventsFunction<InventoryItem> applyEventsFunction() {
        return new MultiMethodApplyEventsFunction<>();
    }

    @Provides
    public DBI dbi() {
        return new DBI(JdbcConnectionPool.create("jdbc:h2:mem:test;MODE=Oracle", "scott", "tiger"));
    }

    @Provides
    public UowSerialization uowSerialization(Kryo kryo) {
        return new UowSerialization(
                (uow) -> {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Output output = new Output(stream);
                    kryo.writeObject(output, uow);
                    output.close(); // Also calls output.flush()
                    return new String(stream.toByteArray());
                },
                (json) ->{
                    byte[] bytes = json.getBytes();
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    Input input = new Input(inputStream);
                    return kryo.readObject(input, UnitOfWork.class);
                });
    }

    @Provides
    public CmdSerialization cmdSerialization(Kryo kryo) {
        return new CmdSerialization(
                (uow) -> {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Output output = new Output(stream);
                    kryo.writeObject(output, uow);
                    output.close(); // Also calls output.flush()
                    return new String(stream.toByteArray());
                },
                (json) ->{
                    byte[] bytes = json.getBytes();
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    Input input = new Input(inputStream);
                    return kryo.readObject(input, Command.class);
                });
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
