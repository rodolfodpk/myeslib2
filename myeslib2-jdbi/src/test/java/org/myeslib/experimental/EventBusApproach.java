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
import org.myeslib.core.storage.SnapshotReader;
import org.myeslib.storage.helpers.DbAwareBaseTestClass;
import org.myeslib.storage.helpers.SampleDomainGsonFactory;
import org.myeslib.storage.helpers.eventsource.SnapshotHelper;
import org.myeslib.storage.jdbi.JdbiSnapshotReader;
import org.myeslib.storage.jdbi.JdbiUuidUnitOfWorkJournal;
import org.myeslib.storage.jdbi.dao.JdbiUuidDao;
import org.myeslib.storage.jdbi.dao.config.AggregateRootDbMetadata;
import org.myeslib.storage.jdbi.dao.config.UowSerializationFunctions;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.myeslib.storage.helpers.SampleDomain.*;

public class EventBusApproach extends DbAwareBaseTestClass {

    Gson gson;
    UowSerializationFunctions functions;
    AggregateRootDbMetadata dbMetadata;
    JdbiUuidDao dao;
    Cache<UUID, Snapshot<InventoryItemAggregateRoot>> cache;
    SnapshotHelper<InventoryItemAggregateRoot> snapshotHelper;
    JdbiSnapshotReader<UUID, InventoryItemAggregateRoot> snapshotReader ;
    JdbiUuidUnitOfWorkJournal journal;


    @BeforeClass
    public static void setup() throws Exception {
        initDb();
    }

    @Before
    public void init() throws Exception {
        gson = new SampleDomainGsonFactory().create();
        functions = new UowSerializationFunctions(
                gson::toJson,
                (json) -> gson.fromJson(json, UnitOfWork.class));
        dbMetadata = new AggregateRootDbMetadata("inventory_item");
        dao = new JdbiUuidDao(functions, dbMetadata, dbi);
        cache = CacheBuilder.newBuilder().maximumSize(1000).build();
        snapshotHelper = new SnapshotHelper<>();
        snapshotReader = new JdbiSnapshotReader<>(InventoryItemAggregateRoot::new, dao, cache, snapshotHelper);
        journal = new JdbiUuidUnitOfWorkJournal(dao);
    }

    @Test
    public void testOneCommand() throws InterruptedException {

        EventBus bus = new EventBus();
        bus.register(new CommandSubscriber(bus, snapshotReader, journal, id -> id.toString()));
        bus.register(new EventSubscriber(bus));

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
    public void testCommandsInBatch() throws InterruptedException {

        EventBus bus = new EventBus();
        bus.register(new CommandSubscriberBatch(bus, snapshotReader, snapshotHelper, journal, id -> id.toString()));
        bus.register(new EventSubscriber(bus));

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


}
class CommandSubscriber {

    final EventBus bus;
    final SnapshotReader snapshotReader;
    final JdbiUuidUnitOfWorkJournal journal;
    final ItemDescriptionGeneratorService service;

    CommandSubscriber(EventBus bus, SnapshotReader snapshotReader, JdbiUuidUnitOfWorkJournal journal, ItemDescriptionGeneratorService service) {
        this.bus = bus;
        this.snapshotReader = snapshotReader;
        this.journal = journal;
        this.service = service;
    }

    @Subscribe
    public void on(CreateInventoryItem command) {
        System.out.println("command " + command);
        Snapshot<InventoryItemAggregateRoot> snapshot = snapshotReader.getSnapshot(command.getId());
        CreateCommandHandler handler = new CreateCommandHandler(service);
        UnitOfWork uow = handler.handle(command, snapshot);
        journal.append(command.getId(), uow);
        // bus.post(uow);
        // TODO publish Id, List<UnitOfWork> instead of events ??!!
    }

}

class CommandSubscriberBatch {

    final EventBus bus;
    final SnapshotReader snapshotReader;
    final SnapshotHelper<InventoryItemAggregateRoot> snapshotHelper;
    final JdbiUuidUnitOfWorkJournal journal;
    final ItemDescriptionGeneratorService service;

    CommandSubscriberBatch(EventBus bus, SnapshotReader snapshotReader,
                           SnapshotHelper<InventoryItemAggregateRoot> snapshotHelper,
                           JdbiUuidUnitOfWorkJournal journal, ItemDescriptionGeneratorService service) {
        this.bus = bus;
        this.snapshotReader = snapshotReader;
        this.snapshotHelper = snapshotHelper;
        this.journal = journal;
        this.service = service;
    }

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

        journal.appendBatch(command.getId(), ImmutableList.of(uow, uow2, uow3));

    }

}



class EventSubscriber {

    final EventBus bus;

    EventSubscriber(EventBus bus) {
        this.bus = bus;
    }

    @Subscribe
    public void on(InventoryItemCreated event) {
    }

}