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
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.storage.helpers.DbAwareBaseTestClass;
import org.myeslib.storage.helpers.SampleDomainGsonFactory;
import org.myeslib.storage.helpers.eventsource.SnapshotHelper;
import org.myeslib.storage.jdbi.JdbiJournal;
import org.myeslib.storage.jdbi.JdbiReader;
import org.myeslib.storage.jdbi.dao.JdbiDao;
import org.myeslib.storage.jdbi.dao.config.DbMetadata;
import org.myeslib.storage.jdbi.dao.config.UowSerialization;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.myeslib.storage.helpers.SampleDomain.*;

public class EventBusApproach extends DbAwareBaseTestClass {

    Gson gson;
    UowSerialization functions;
    DbMetadata dbMetadata;
    JdbiDao<UUID> dao;
    Cache<UUID, Snapshot<InventoryItemAggregateRoot>> cache;
    SnapshotHelper<InventoryItemAggregateRoot> snapshotHelper;
    JdbiReader<UUID, InventoryItemAggregateRoot> snapshotReader ;
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
        snapshotReader = new JdbiReader<>(() -> new InventoryItemAggregateRoot(), dao, cache, snapshotHelper);
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

        InventoryItemAggregateRoot expected = new InventoryItemAggregateRoot();
        expected.setId(key);
        expected.setDescription(key.toString());
        expected.setAvailable(0);
        Snapshot<InventoryItemAggregateRoot> expectedSnapshot = new Snapshot<>(expected, 1L);

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

        InventoryItemAggregateRoot expected = new InventoryItemAggregateRoot();
        expected.setId(key);
        expected.setDescription(key.toString());
        expected.setAvailable(0);
        Snapshot<InventoryItemAggregateRoot> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(key), is(expectedSnapshot));

    }

    @Test
    public void commandsInBatch() throws InterruptedException {

        bus.register(new CommandSubscriberBatch());

        // create

        UUID key = UUID.randomUUID() ;
        CreateInventoryItem command1 = new CreateInventoryItem(UUID.randomUUID(), key);
        bus.post(command1);

        InventoryItemAggregateRoot expected = new InventoryItemAggregateRoot();
        expected.setId(key);
        expected.setDescription(key.toString());
        expected.setAvailable(1);
        Snapshot<InventoryItemAggregateRoot> expectedSnapshot = new Snapshot<>(expected, 3L);

        assertThat(snapshotReader.getSnapshot(key), is(expectedSnapshot));

    }

    @Test
    public void aCommandWithManyEvents() throws InterruptedException {

        bus.register(new CommandSubscriber());

        // create then increase and decrease

        UUID key = UUID.randomUUID() ;
        CreateInventoryItemThenIncreaseAndDecrease command1 = new CreateInventoryItemThenIncreaseAndDecrease(UUID.randomUUID(), key, 2, 1);
        bus.post(command1);

        InventoryItemAggregateRoot expected = new InventoryItemAggregateRoot();
        expected.setId(key);
        expected.setDescription(key.toString());
        expected.setAvailable(1);
        Snapshot<InventoryItemAggregateRoot> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(key), is(expectedSnapshot));

    }

    @Test
    public void howToCaptureEventsFromCaller() throws InterruptedException {

        EventBus bus = new EventBus();
        StateFullCommandSubscriber commandSubscriber = new StateFullCommandSubscriber();
        bus.register(commandSubscriber);
        bus.register(new EventSubscriber());

        UUID key = UUID.randomUUID() ;
        CreateInventoryItemThenIncreaseAndDecrease command1 = new CreateInventoryItemThenIncreaseAndDecrease(UUID.randomUUID(), key, 2, 1);
        bus.post(command1);

        InventoryItemAggregateRoot expected = new InventoryItemAggregateRoot();
        expected.setId(key);
        expected.setDescription(key.toString());
        expected.setAvailable(1);
        Snapshot<InventoryItemAggregateRoot> expectedSnapshot = new Snapshot<>(expected, 1L);

        assertThat(snapshotReader.getSnapshot(key), is(expectedSnapshot));

        assertThat(commandSubscriber.transactions.size(), is(1));

        assertThat(commandSubscriber.transactions.get(0).getEvents().size(), is(3));

    }

    class CommandSubscriber {

        @Subscribe
        public void on(CreateInventoryItem command) {
            System.out.println("command " + command);
            Snapshot<InventoryItemAggregateRoot> snapshot = snapshotReader.getSnapshot(command.getId());
            CreateCommandHandler handler = new CreateCommandHandler(service);
            UnitOfWork uow = handler.handle(command, snapshot);
            journal.append(command.getId(), uow);
        }

        @Subscribe
        public void on(IncreaseInventory command) {
            System.out.println("command " + command);
            Snapshot<InventoryItemAggregateRoot> snapshot = snapshotReader.getSnapshot(command.getId());
            UnitOfWork uow = new IncreaseCommandHandler().handle(command, snapshot);
            journal.append(command.getId(), uow);
        }

        @Subscribe
        public void on(CreateInventoryItemThenIncreaseAndDecrease command) {
            System.out.println("command " + command);
            Snapshot<InventoryItemAggregateRoot> snapshot = snapshotReader.getSnapshot(command.getId());
            UnitOfWork uow = new CreateThenIncreaseAndDecreaseCommandHandler(service).handle(command, snapshot);
            journal.append(command.getId(), uow);
        }

    }

    class StateFullCommandSubscriber  {

        final List<UnitOfWork> transactions = new ArrayList<>();

        @Subscribe
        public void on(CreateInventoryItemThenIncreaseAndDecrease command) {
            System.out.println("command " + command);
            Snapshot<InventoryItemAggregateRoot> snapshot = snapshotReader.getSnapshot(command.getId());
            UnitOfWork uow = new CreateThenIncreaseAndDecreaseCommandHandler(service).handle(command, snapshot);
            journal.append(command.getId(), uow);
            transactions.add(uow);
        }

    }

    class CommandSubscriberBatch {

        @Subscribe
        public void on(CreateInventoryItem command) {

            UUID id = command.getId();

            Snapshot<InventoryItemAggregateRoot> initialSnapshot = snapshotReader.getSnapshot(command.getId());
            CreateCommandHandler handler = new CreateCommandHandler(service);
            UnitOfWork uow = handler.handle(command, initialSnapshot);

            Snapshot<InventoryItemAggregateRoot> afterFirstCommandSnapshot = snapshotHelper.applyEventsOn(initialSnapshot.getAggregateInstance(), uow);
            IncreaseInventory command2 = new IncreaseInventory(UUID.randomUUID(), id, 3, 1L);
            UnitOfWork uow2 = new IncreaseCommandHandler().handle(command2, afterFirstCommandSnapshot);

            Snapshot<InventoryItemAggregateRoot> afterSecondCommandSnapshot = snapshotHelper.applyEventsOn(afterFirstCommandSnapshot.getAggregateInstance(), uow2);
            DecreaseInventory command3 = new DecreaseInventory(UUID.randomUUID(), id, 2, 2L);
            UnitOfWork uow3 = new DecreaseCommandHandler().handle(command3, afterSecondCommandSnapshot);

            journal.appendBatch(id, ImmutableList.of(uow, uow2, uow3));

        }

    }

    class EventSubscriber {

        @Subscribe
        public void on(InventoryItemCreated event) {
        }

    }
}



