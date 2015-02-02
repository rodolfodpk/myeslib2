package org.myeslib.sampledomain;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myeslib.core.Command;
import org.myeslib.core.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.ApplyEventsFunction;
import org.myeslib.jdbi.infra.JdbiReader;
import org.myeslib.jdbi.infra.helpers.DbAwareBaseTestClass;
import org.myeslib.jdbi.infra.JdbiJournal;
import org.myeslib.jdbi.infra.MultiMethodApplyEventsFunction;
import org.myeslib.jdbi.infra.dao.JdbiDao;
import org.myeslib.jdbi.infra.dao.config.CmdSerialization;
import org.myeslib.jdbi.infra.dao.config.DbMetadata;
import org.myeslib.jdbi.infra.dao.config.UowSerialization;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.EventsGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SampleDomainTest extends DbAwareBaseTestClass {

    static final Logger logger = LoggerFactory.getLogger(SampleDomainTest.class);

    Gson gson;
    UowSerialization functions;
    CmdSerialization cmdSer;
    DbMetadata dbMetadata;
    JdbiDao<UUID> dao;
    Cache<UUID, Snapshot<InventoryItem>> cache;
    ApplyEventsFunction<InventoryItem> applyEventsFunction;
    JdbiReader<UUID, InventoryItem> snapshotReader ;
    JdbiJournal<UUID> journal;
    SampleDomainService service;

    @BeforeClass
    public static void setup() throws Exception {
        initDb();
    }

    @Before
    public void init() throws Exception {
        gson = new EventsGsonFactory().create();
        functions = new UowSerialization(
                gson::toJson,
                (json) -> gson.fromJson(json, UnitOfWork.class));
        cmdSer = new CmdSerialization(
                (cmd) -> gson.toJson(cmd, Command.class),
                (json) -> gson.fromJson(json, Command.class));
        dbMetadata = new DbMetadata("inventory_item");
        dao = new JdbiDao<>(functions, cmdSer, dbMetadata, dbi);
        cache = CacheBuilder.newBuilder().maximumSize(1000).build();
        applyEventsFunction = new MultiMethodApplyEventsFunction<>();
        snapshotReader = new JdbiReader<>(() -> InventoryItem.builder().build(), dao, cache, applyEventsFunction);
        journal = new JdbiJournal<>(dao);
        service = id -> id.toString();
    }

    @Test
    public void testCreateInventoryItemHandler() throws InterruptedException {

        // create

        UUID itemId = UUID.randomUUID() ;
        CreateInventoryItem command =  CreateInventoryItem.create(UUID.randomUUID(), itemId);

        CreateInventoryItemHandler handler = new CreateInventoryItemHandler(service, journal, snapshotReader);
        handler.handle(command);

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(0).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(itemId), is(expectedSnapshot));

    }

    @Test
    public void testCreateThenIncreaseThenDecreaseHandler() throws InterruptedException {

        // command to create then increase and decrease

        UUID itemId = UUID.randomUUID() ;
        CreateInventoryItemThenIncreaseThenDecrease command = CreateInventoryItemThenIncreaseThenDecrease.create(UUID.randomUUID(), itemId, 2, 1);

        new CreateThenIncreaseThenDecreaseHandler(service, journal, snapshotReader).handle(command);

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(1).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(itemId), is(expectedSnapshot));

    }

    class EventsSubscriberToReflectQueryModel {

        @Subscribe
        public void on(UnitOfWork uow) {

            logger.info("received a UnitOfWork with {} events:" + uow.getEvents().size());
            for (Event e: uow.getEvents()) {
                logger.info("  {}", e);
            }

        }

    }
}
