package org.myeslib;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import org.h2.jdbcx.JdbcConnectionPool;
import org.myeslib.data.Command;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.Snapshot;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.WriteModelJournal;
import org.myeslib.infra.dao.config.CmdSerialization;
import org.myeslib.infra.dao.config.DbMetadata;
import org.myeslib.infra.dao.config.UowSerialization;
import org.myeslib.stack1.infra.HazelcastCacheFactory;
import org.myeslib.stack1.infra.Stack1JCacheReader;
import org.myeslib.stack1.infra.Stack1Journal;
import org.myeslib.stack1.infra.dao.Stack1JdbiDao;
import org.myeslib.stack1.infra.helpers.DatabaseHelper;
import org.skife.jdbi.v2.DBI;
import sampledomain.aggregates.inventoryitem.InventoryItem;
import sampledomain.aggregates.inventoryitem.commands.CommandsGsonFactory;
import sampledomain.aggregates.inventoryitem.events.EventsGsonFactory;

import java.util.UUID;

public class InventoryItemHazelcastModule extends AbstractModule {

    final HazelcastCacheFactory<UUID, InventoryItem> factory =  new HazelcastCacheFactory<>();

    final javax.cache.Cache<UUID, Snapshot<InventoryItem>> jcache = factory.cache(UUID.randomUUID().toString(), UUID.class, InventoryItem.class);

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
    public UowSerialization<InventoryItem> uowSerialization(@Named("events-json") Gson gson) {
        return new UowSerialization<>(
                (uow) -> gson.toJson(uow),
                (json) -> gson.fromJson(json, UnitOfWork.class));
    }

    @Provides
    @Singleton
    public CmdSerialization<InventoryItem> cmdSerialization(@Named("commands-json") Gson gson) {
        return new CmdSerialization<>(
                (cmd) -> gson.toJson(cmd, Command.class),
                (json) -> gson.fromJson(json, Command.class));
    }

    @Provides
    @Singleton
    public DBI dbi() {
        return new DBI(JdbcConnectionPool.create("jdbc:h2:mem:test;MODE=Oracle", "scott", "tiger"));
    }

    @Provides
    @Singleton
    public DatabaseHelper databaseHelper(DBI dbi){
        return new DatabaseHelper(dbi, "database/V1__Create_inventory_item_tables.sql");
    }

    @Override
    protected void configure() {

        bind(new TypeLiteral<DbMetadata<InventoryItem>>() {}).toInstance(new DbMetadata<>("inventory_item"));

        bind(new TypeLiteral<WriteModelDao<UUID, InventoryItem>>() {})
                .to(new TypeLiteral<Stack1JdbiDao<UUID, InventoryItem>>() {}).asEagerSingleton();

        bind(new TypeLiteral<WriteModelJournal<UUID, InventoryItem>>() {})
                .to(new TypeLiteral<Stack1Journal<UUID, InventoryItem>>() {}).asEagerSingleton();

        bind(new TypeLiteral<SnapshotReader<UUID, InventoryItem>>() {})
                .to(new TypeLiteral<Stack1JCacheReader<UUID, InventoryItem>>() {}).asEagerSingleton();

        final HazelcastCacheFactory<UUID, InventoryItem> factory =  new HazelcastCacheFactory<>();

        javax.cache.Cache<UUID, Snapshot<InventoryItem>> jcache = factory.cache(UUID.randomUUID().toString(), UUID.class, InventoryItem.class);

        bind(new TypeLiteral<javax.cache.Cache<UUID, Snapshot<InventoryItem>>>() {
        })
                .toInstance(jcache);

    }
}
