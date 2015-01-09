package org.myeslib.experimental;

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
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.*;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.domain.SampleDomainGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands.HandleCreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands.HandleCreateThenIncreaseThenDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands.HandleDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.comands.HandleIncrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.services.SampleDomainService;
import org.myeslib.storage.helpers.DbAwareBaseTestClass;
import org.myeslib.storage.helpers.eventsource.SnapshotHelper;
import org.myeslib.storage.jdbi.JdbiJournal;
import org.myeslib.storage.jdbi.JdbiReader;
import org.myeslib.storage.jdbi.dao.JdbiDao;
import org.myeslib.storage.jdbi.dao.config.DbMetadata;
import org.myeslib.storage.jdbi.dao.config.UowSerialization;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EventBusApproach extends DbAwareBaseTestClass {

    Gson gson;
    UowSerialization functions;
    DbMetadata dbMetadata;
    JdbiDao<UUID> dao;
    Cache<UUID, Snapshot<InventoryItem>> cache;
    SnapshotHelper<InventoryItem> snapshotHelper;
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
        snapshotHelper = new SnapshotHelper<>();
        snapshotReader = new JdbiReader<>(() -> InventoryItem.builder().build(), dao, cache, snapshotHelper);
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

        bus.register(new CommandSubscriber());

        // create
        UUID key = UUID.randomUUID() ;
        CreateInventoryItem validCommand = new CreateInventoryItem(UUID.randomUUID(), key);
        bus.post(validCommand);

        // now increase (will fail since it has an invalid targetVersion)
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

        UUID key = UUID.randomUUID() ;
        CreateInventoryItemThenIncreaseThenDecrease command1 = new CreateInventoryItemThenIncreaseThenDecrease(UUID.randomUUID(), key, 2, 1);
        bus.post(command1);

        InventoryItem expected = InventoryItem.builder().id(key).description(key.toString()).available(1).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(key), is(expectedSnapshot));

    }

    @Test
    public void howToCaptureEventsFromCaller() throws InterruptedException {

        EventBus bus = new EventBus();
        StateFullCommandSubscriber commandSubscriber = new StateFullCommandSubscriber();
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
            System.out.println("command " + command);
            Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getId());
            HandleCreateInventoryItem handler = new HandleCreateInventoryItem(service);
            UnitOfWork uow = handler.handle(command, snapshot);
            journal.append(command.getId(), uow);
        }

        @Subscribe
        public void on(IncreaseInventory command) {
            System.out.println("command " + command);
            Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getId());
            UnitOfWork uow = new HandleIncrease().handle(command, snapshot);
            journal.append(command.getId(), uow);
        }

        @Subscribe
        public void on(CreateInventoryItemThenIncreaseThenDecrease command) {
            System.out.println("command " + command);
            Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getId());
            UnitOfWork uow = new HandleCreateThenIncreaseThenDecrease(service).handle(command, snapshot);
            journal.append(command.getId(), uow);
        }

    }

    class CommandSubscriberBatch {

        @Subscribe
        public void on(CreateInventoryItem command) {

            UUID id = command.getId();

            Snapshot<InventoryItem> initialSnapshot = snapshotReader.getSnapshot(command.getId());
            HandleCreateInventoryItem handler = new HandleCreateInventoryItem(service);
            UnitOfWork uow = handler.handle(command, initialSnapshot);

            Snapshot<InventoryItem> afterFirstCommandSnapshot = snapshotHelper.applyEventsOn(initialSnapshot.getAggregateInstance(), uow);
            IncreaseInventory command2 = new IncreaseInventory(UUID.randomUUID(), id, 10, 1L);
            UnitOfWork uow2 = new HandleIncrease().handle(command2, afterFirstCommandSnapshot);

            Snapshot<InventoryItem> afterSecondCommandSnapshot = snapshotHelper.applyEventsOn(afterFirstCommandSnapshot.getAggregateInstance(), uow2);
            DecreaseInventory command3 = new DecreaseInventory(UUID.randomUUID(), id, 2, 2L);
            UnitOfWork uow3 = new HandleDecrease().handle(command3, afterSecondCommandSnapshot);

            journal.appendBatch(id, ImmutableList.of(uow, uow2, uow3));

        }

    }

    class StateFullCommandSubscriber  {

        final AtomicReference<UnitOfWork> transaction = new AtomicReference<>();

        @Subscribe
        public void on(CreateInventoryItemThenIncreaseThenDecrease command) {
            System.out.println("command " + command);
            Snapshot<InventoryItem> snapshot = snapshotReader.getSnapshot(command.getId());
            UnitOfWork uow = new HandleCreateThenIncreaseThenDecrease(service).handle(command, snapshot);
            journal.append(command.getId(), uow);
            transaction.set(uow);
        }

    }

    class EventsSubscriberToReflectQueryModel {

        @Subscribe
        public void on(UnitOfWork uow) {

            System.out.println("received a UnitOfWork with " + uow.getEvents().size() + " events: ");
            for (Event e: uow.getEvents()) {
                System.out.println("  "+ e);
            }

        }

    }
}



