package org.myeslib.sampledomain;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myeslib.core.Command;
import org.myeslib.core.Event;
import org.myeslib.data.CommandResults;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.function.ApplyEventsFunction;
import org.myeslib.jdbi.function.multimethod.MultiMethodApplyEventsFunction;
import org.myeslib.jdbi.storage.JdbiJournal;
import org.myeslib.jdbi.storage.JdbiReader;
import org.myeslib.jdbi.storage.dao.JdbiDao;
import org.myeslib.jdbi.storage.dao.config.CommandSerialization;
import org.myeslib.jdbi.storage.dao.config.DbMetadata;
import org.myeslib.jdbi.storage.dao.config.UowSerialization;
import org.myeslib.jdbi.storage.helpers.DbAwareBaseTestClass;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.SampleDomainGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SampleDomainTest extends DbAwareBaseTestClass {

    static final Logger logger = LoggerFactory.getLogger(SampleDomainTest.class);

    Gson gson;
    UowSerialization functions;
    CommandSerialization<UUID> cmdSer;
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
        gson = new SampleDomainGsonFactory().create();
        functions = new UowSerialization(
                gson::toJson,
                (json) -> gson.fromJson(json, UnitOfWork.class));
        Type type = new TypeToken<Command<UUID>>(){}.getType();
        cmdSer = new CommandSerialization<UUID>(
                (cmd) -> gson.toJson(cmd, type),
                (json) -> gson.fromJson(json, type));
        dbMetadata = new DbMetadata("inventory_item");
        dao = new JdbiDao<>(functions, cmdSer, dbMetadata, dbi);
        cache = CacheBuilder.newBuilder().maximumSize(1000).build();
        applyEventsFunction = new MultiMethodApplyEventsFunction<>();
        snapshotReader = new JdbiReader<>(() -> InventoryItem.builder().build(), dao, cache, applyEventsFunction);
        journal = new JdbiJournal<>(dao);
        service = id -> id.toString();
    }

    @Test
    public void oneCommandShouldWork() throws InterruptedException {

        // create

        UUID itemId = UUID.randomUUID() ;
        CreateInventoryItem command = new CreateInventoryItem(UUID.randomUUID(), itemId);

        Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getTargetId());
        CreateInventoryItemHandler handler = new CreateInventoryItemHandler(service);
        UnitOfWork unitOfWork = handler.handle(command, snapshot);
        journal.append(command, unitOfWork);

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(0).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(itemId), is(expectedSnapshot));

    }

    @Test(expected = Exception.class)
    public void validCommandPlusInvalidCommand() throws InterruptedException {

        UUID itemId = UUID.randomUUID() ;

        // create
        CreateInventoryItem validCommand = new CreateInventoryItem(UUID.randomUUID(), itemId);
        Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(validCommand.getTargetId());
        CreateInventoryItemHandler handler = new CreateInventoryItemHandler(service);
        UnitOfWork unitOfWork = handler.handle(validCommand, snapshot);
        journal.append(validCommand, unitOfWork);

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(0).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        // now we have version = 1
        assertThat(snapshotReader.getSnapshot(itemId), is(expectedSnapshot));

        // now increase (will fail since it has targetVersion = 0 instead of 1)
        IncreaseInventory invalidCommand = new IncreaseInventory(UUID.randomUUID(), itemId, 3);
        UnitOfWork willFail = new IncreaseHandler().handle(invalidCommand, snapshot);
        // since IncreaseHandler is using the same snapshot we used on first operation, the next line will fail
        journal.append(invalidCommand, willFail);

    }

    @Test
    public void aCommandWithManyEvents() throws InterruptedException {

        // command to create then increase and decrease

        UUID itemId = UUID.randomUUID() ;
        CreateInventoryItemThenIncreaseThenDecrease command = new CreateInventoryItemThenIncreaseThenDecrease(UUID.randomUUID(), itemId, 2, 1);

        Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getTargetId());
        UnitOfWork results = new CreateThenIncreaseThenDecreaseHandler(service).handle(command, snapshot);
        journal.append(command, results);

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
