package org.myeslib.sampledomain;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
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
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.*;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.domain.SampleDomainGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands.HandleCreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands.HandleCreateThenIncreaseThenDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands.HandleDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands.HandleIncrease;
import org.myeslib.sampledomain.services.SampleDomainService;
import org.myeslib.jdbi.storage.helpers.DbAwareBaseTestClass;
import org.myeslib.jdbi.storage.JdbiJournal;
import org.myeslib.jdbi.storage.JdbiReader;
import org.myeslib.jdbi.storage.dao.JdbiDao;
import org.myeslib.jdbi.storage.dao.config.DbMetadata;
import org.myeslib.jdbi.storage.dao.config.UowSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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
    EventBus bus ;
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
        bus = new EventBus();
        service = id -> id.toString();
    }

    @Test
    public void oneCommandShouldWork() throws InterruptedException {

        bus.register(new CommandSubscriber());

        // create

        UUID key = UUID.randomUUID() ;
        CreateInventoryItem command1 = new CreateInventoryItem(UUID.randomUUID(), key);
        bus.post(command1);

        InventoryItem expected = InventoryItem.builder().id(key).description(key.toString()).available(0).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(key), is(expectedSnapshot));

    }

    @Test
    public void validCommandPlusInvalidCommand() throws InterruptedException {

        UUID key = UUID.randomUUID() ;

        bus.register(new CommandSubscriber());

        // create
        CreateInventoryItem validCommand = new CreateInventoryItem(UUID.randomUUID(), key);
        bus.post(validCommand);
        // now we have version = 1

        // now increase (will fail since it has targetVersion = 0 instead of 1)
        IncreaseInventory invalidCommand = new IncreaseInventory(UUID.randomUUID(), key, 3, 0L); // note 0L as an invalid targetVersion
        bus.post(invalidCommand);

        InventoryItem expected = InventoryItem.builder().id(key).description(key.toString()).available(0).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(key), is(expectedSnapshot));

    }

    @Test
    public void commandsInBatch() throws InterruptedException {

        bus.register(new CommandSubscriberBatch());

        // create

        UUID key = UUID.randomUUID() ;
        CreateInventoryItem command1 = new CreateInventoryItem(UUID.randomUUID(), key);
        bus.post(command1);

        InventoryItem expected = InventoryItem.builder().id(key).description(key.toString()).available(8).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 3L);

        assertThat(snapshotReader.getSnapshot(key), is(expectedSnapshot));

    }

    @Test
    public void aCommandWithManyEvents() throws InterruptedException {

        bus.register(new CommandSubscriber());

        // create then increase and decrease

        UUID itemId = UUID.randomUUID() ;
        CreateInventoryItemThenIncreaseThenDecrease command1 = new CreateInventoryItemThenIncreaseThenDecrease(UUID.randomUUID(), itemId, 2, 1);
        bus.post(command1);

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(1).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

       assertThat(snapshotReader.getSnapshot(itemId), is(expectedSnapshot));

    }

    @Test
    public void howToCaptureEventsFromCaller() throws InterruptedException {

        EventBus bus = new EventBus();
        StatefulCommandSubscriber commandSubscriber = new StatefulCommandSubscriber();
        bus.register(commandSubscriber);

        EventBus bus2 = new EventBus();
        bus2.register(new EventsSubscriberToReflectQueryModel());

        UUID key = UUID.randomUUID() ;
        CreateInventoryItemThenIncreaseThenDecrease command1 = new CreateInventoryItemThenIncreaseThenDecrease(UUID.randomUUID(), key, 2, 1);
        bus.post(command1);

        InventoryItem expected = InventoryItem.builder().id(key).description(key.toString()).available(1).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(key), is(expectedSnapshot));

        assertThat(commandSubscriber.transaction.get().getEvents().size(), is(3));

        bus2.post(commandSubscriber.transaction);

    }

    class CommandSubscriber {

        @Subscribe
        public void on(CreateInventoryItem command) {
            try {
                logger.info("command {}", command);
                Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getId());
                HandleCreateInventoryItem handler = new HandleCreateInventoryItem(service);
                UnitOfWork uow = handler.handle(command, snapshot);
                journal.append(command.getId(), uow);
            } catch (Throwable t) {
                // TODO to generate an error notification (event ?) to the caller
            }

        }

        @Subscribe
        public void on(IncreaseInventory command) {
            logger.info("command {}", command);
            Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getId());
            UnitOfWork uow = new HandleIncrease().handle(command, snapshot);
            journal.append(command.getId(), uow);
        }

        @Subscribe
        public void on(CreateInventoryItemThenIncreaseThenDecrease command) {
            try {
                logger.info("command {}", command);
                Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getId());
                UnitOfWork uow = new HandleCreateThenIncreaseThenDecrease(service, snapshotComputing).handle(command, snapshot);
                journal.append(command.getId(), uow);
            } catch (Throwable t) {
                t.printStackTrace();
            }

        }

    }

    class CommandSubscriberBatch {

        @Subscribe
        public void on(CreateInventoryItem command) {

            UUID id = command.getId();

            Snapshot<InventoryItem> initialSnapshot = snapshotReader.getSnapshot(command.getId());
            HandleCreateInventoryItem handler = new HandleCreateInventoryItem(service);
            UnitOfWork uow = handler.handle(command, initialSnapshot);

            Snapshot<InventoryItem> afterFirstCommandSnapshot = snapshotComputing.applyEventsOn(initialSnapshot.getAggregateInstance(), uow);
            IncreaseInventory command2 = new IncreaseInventory(UUID.randomUUID(), id, 10, 1L);
            UnitOfWork uow2 = new HandleIncrease().handle(command2, afterFirstCommandSnapshot);

            Snapshot<InventoryItem> afterSecondCommandSnapshot = snapshotComputing.applyEventsOn(afterFirstCommandSnapshot.getAggregateInstance(), uow2);
            DecreaseInventory command3 = new DecreaseInventory(UUID.randomUUID(), id, 2, 2L);
            UnitOfWork uow3 = new HandleDecrease().handle(command3, afterSecondCommandSnapshot);

            journal.appendBatch(id, ImmutableList.of(uow, uow2, uow3));

        }

    }

    class StatefulCommandSubscriber {

        final AtomicReference<UnitOfWork> transaction = new AtomicReference<>();

        @Subscribe
        public void on(CreateInventoryItemThenIncreaseThenDecrease command) {
            logger.info("command {}", command);
            Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getId());
            UnitOfWork uow = new HandleCreateThenIncreaseThenDecrease(service, snapshotComputing).handle(command, snapshot);
            journal.append(command.getId(), uow);
            transaction.set(uow);
        }

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



