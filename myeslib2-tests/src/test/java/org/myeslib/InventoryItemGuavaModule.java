package org.myeslib;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import org.myeslib.infra.Snapshot;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.WriteModelJournal;
import org.myeslib.stack1.infra.Stack1Journal;
import org.myeslib.stack1.infra.Stack1Reader;
import org.myeslib.stack1.infra.dao.Stack1MemDao;
import sampledomain.aggregates.inventoryitem.InventoryItem;

import java.util.UUID;

public class InventoryItemGuavaModule extends AbstractModule {

    @Provides
    @Singleton
    Cache<UUID, Snapshot<InventoryItem>> cache(){
        return CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    @Override
    protected void configure() {

        bind(new TypeLiteral<WriteModelDao<UUID, InventoryItem>>() {})
                .to(new TypeLiteral<Stack1MemDao<UUID, InventoryItem>>() {}).asEagerSingleton();

        bind(new TypeLiteral<SnapshotReader<UUID, InventoryItem>>() {})
                .to(new TypeLiteral<Stack1Reader<UUID, InventoryItem>>() {}).asEagerSingleton();

        bind(new TypeLiteral<WriteModelJournal<UUID, InventoryItem>>() {})
                .to(new TypeLiteral<Stack1Journal<UUID, InventoryItem>>() {}).asEagerSingleton();

    }
}
