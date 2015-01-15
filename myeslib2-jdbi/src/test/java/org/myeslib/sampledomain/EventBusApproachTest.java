package org.myeslib.sampledomain;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myeslib.core.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.SnapshotComputing;
import org.myeslib.jdbi.function.MutableSnapshotComputing;
import org.myeslib.jdbi.storage.JdbiJournal;
import org.myeslib.jdbi.storage.JdbiReader;
import org.myeslib.jdbi.storage.dao.JdbiDao;
import org.myeslib.jdbi.storage.dao.config.DbMetadata;
import org.myeslib.jdbi.storage.dao.config.UowSerialization;
import org.myeslib.jdbi.storage.helpers.DbAwareBaseTestClass;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.SampleDomainGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.DecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EventBusApproachTest extends DbAwareBaseTestClass {

    static final Logger logger = LoggerFactory.getLogger(EventBusApproachTest.class);

    Gson gson;
    UowSerialization functions;
    DbMetadata dbMetadata;
    JdbiDao<UUID> dao;
    Cache<UUID, Snapshot<InventoryItem>> cache;
    SnapshotComputing<InventoryItem> snapshotComputing;
    JdbiReader<UUID, InventoryItem> snapshotReader ;
    JdbiJournal<UUID> journal;
    SampleDomainService service;

    @BeforeClass
    public static void setup() throws Exception {
        initDb();
    }

    @Before
    public void init() throws Exception {
        gson = new SampleDomainGsonFactory().create();
        functions = new UowSerialization(
                gson::toJson,
                (json) -> gson.fromJson(json, UnitOfWork.class));
        dbMetadata = new DbMetadata("inventory_item");
        dao = new JdbiDao<>(functions, dbMetadata, dbi);
        cache = CacheBuilder.newBuilder().maximumSize(1000).build();
        snapshotComputing = new MutableSnapshotComputing<>();
        snapshotReader = new JdbiReader<>(() -> InventoryItem.builder().build(), dao, cache, snapshotComputing);
        journal = new JdbiJournal<>(dao);
        service = id -> id.toString();
    }

    @Test
    public void oneCommandShouldWork() throws InterruptedException {

        // create

        UUID itemId = UUID.randomUUID() ;
        CreateInventoryItem command = new CreateInventoryItem(UUID.randomUUID(), itemId);

        Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getId());
        CreateInventoryItemHandler handler = new CreateInventoryItemHandler(service);
        UnitOfWork uow = handler.handle(command, snapshot).getUnitOfWork();
        journal.append(command.getId(), uow);

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(0).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(itemId), is(expectedSnapshot));

    }

    @Test(expected = Exception.class)
    public void validCommandPlusInvalidCommand() throws InterruptedException {

        UUID itemId = UUID.randomUUID() ;

        // create
        CreateInventoryItem validCommand = new CreateInventoryItem(UUID.randomUUID(), itemId);
        Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(validCommand.getId());
        CreateInventoryItemHandler handler = new CreateInventoryItemHandler(service);
        UnitOfWork uow = handler.handle(validCommand, snapshot).getUnitOfWork();
        journal.append(validCommand.getId(), uow);

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(0).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        // now we have version = 1
        assertThat(snapshotReader.getSnapshot(itemId), is(expectedSnapshot));

        // now increase (will fail since it has targetVersion = 0 instead of 1)
        IncreaseInventory invalidCommand = new IncreaseInventory(UUID.randomUUID(), itemId, 3, 0L); // note 0L as an invalid targetVersion
        UnitOfWork uowWillFail = new IncreaseHandler().handle(invalidCommand, expectedSnapshot).getUnitOfWork();
        journal.append(invalidCommand.getId(), uowWillFail);

    }

    @Test
    public void aCommandWithManyEvents() throws InterruptedException {

        // command to create then increase and decrease

        UUID itemId = UUID.randomUUID() ;
        CreateInventoryItemThenIncreaseThenDecrease command = new CreateInventoryItemThenIncreaseThenDecrease(UUID.randomUUID(), itemId, 2, 1);

        Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getId());
        UnitOfWork uow = new CreateThenIncreaseThenDecreaseHandler(service).handle(command, snapshot).getUnitOfWork();
        journal.append(command.getId(), uow);

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(1).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(itemId), is(expectedSnapshot));

    }

    @Test
    public void commandsInBatch() throws InterruptedException {

        // create

        UUID itemId = UUID.randomUUID() ;
        Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(itemId);

        CreateInventoryItem command1 = new CreateInventoryItem(UUID.randomUUID(), itemId);
        UnitOfWork uow = new CreateInventoryItemHandler(service).handle(command1, snapshot).getUnitOfWork();

        IncreaseInventory command2 = new IncreaseInventory(UUID.randomUUID(), itemId, 10, 1L);
        UnitOfWork uow2 = new IncreaseHandler().handle(command2, snapshot).getUnitOfWork();

        DecreaseInventory command3 = new DecreaseInventory(UUID.randomUUID(), itemId, 2, 2L);
        UnitOfWork uow3 = new DecreaseHandler().handle(command3, snapshot).getUnitOfWork();

        journal.appendBatch(itemId, ImmutableList.of(uow, uow2, uow3));

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(8).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 3L);

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
